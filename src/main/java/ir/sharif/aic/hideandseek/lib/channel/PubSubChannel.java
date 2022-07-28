package ir.sharif.aic.hideandseek.lib.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PubSubChannel<T> implements Channel<T> {
    private final List<Watcher<T>> watchers;
    private final Queue<T> eventQueue;
    private final Object queueLock;
    private final AtomicBoolean isClosed;
    private final Thread backgroundBroadcaster;

    public PubSubChannel() {
        this.watchers = Collections.synchronizedList(new ArrayList<>());
        this.eventQueue = new LinkedBlockingQueue<>();
        this.queueLock = new Object();
        this.isClosed = new AtomicBoolean();

        // start the background broadcaster thread
        this.backgroundBroadcaster = new Thread(this::broadcast);
        this.backgroundBroadcaster.start();
    }

    @Override
    public void addWatcher(Watcher<T> watcher) {
        new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.run();
        this.watchers.add(watcher);

    }

    @Override
    public void push(T msg) {
        // if the channel is closed
        if (this.isClosed.get()) {
            // ignore the message
            return;
        }

        this.eventQueue.add(msg);
        synchronized (this.queueLock) {
            this.queueLock.notifyAll();
        }
    }

    @Override
    public void close() {
        this.isClosed.set(true);
        try {
            this.backgroundBroadcaster.join();
        } catch (InterruptedException ignored) {
            // the background broadcaster will never be interrupted
        }
    }

    private void broadcast() {
        while (!this.isClosed.get()) {
            if (this.eventQueue.isEmpty()) {
                // wait for a new message
                synchronized (this.queueLock) {
                    try {
                        this.queueLock.wait(200);
                    } catch (InterruptedException ignored) {
                        // this thread will never be interrupted
                    }
                }
            }
            this.processLatestEvent();
        }

        while (!this.eventQueue.isEmpty()) {
            this.processLatestEvent();
        }
    }

    private void processLatestEvent() {
        if (this.eventQueue.isEmpty()) {
            return;
        }

        var msg = this.eventQueue.poll();
        var tasks = this.startWatchTasks(msg);
        this.waitFor(tasks);
    }

    private List<Thread> startWatchTasks(T msg) {
        var tasks = new ArrayList<Thread>();
        for (Watcher<T> watcher : this.watchers) {
            var task =
                    new Thread(
                            () -> {
                                try {
                                    watcher.watch(msg);
                                } catch (Exception ignored) {
                                    // if a watcher throws an error, it means it kinda denied the message
                                    // therefore we ignore it anyways.
                                }
                            });
            tasks.add(task);
            task.start();
        }
        return tasks;
    }

    private void waitFor(List<Thread> tasks) {
        for (Thread task : tasks) {
            try {
                task.join();
            } catch (InterruptedException ignored) {
                // if a watch task is interrupted, it means that the watcher denied the message
                // therefore we ignore it anyways.
            }
        }
    }
}
