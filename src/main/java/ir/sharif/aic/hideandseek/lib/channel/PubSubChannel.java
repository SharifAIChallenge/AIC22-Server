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

  private void broadcast() {
    while (!this.isClosed.get() || !this.eventQueue.isEmpty()) {

      if (this.eventQueue.isEmpty()) {
        // wait for a new message
        synchronized (this.queueLock) {
          try {
            this.queueLock.wait();
          } catch (InterruptedException ignored) {
            // the queue will never be interrupted
          }
        }
      }

      var msg = this.eventQueue.poll();
      var tasks = this.startWatchTasks(msg);
      this.waitFor(tasks);
    }
  }

  @Override
  public void close() {
    try {
      this.backgroundBroadcaster.join();
    } catch (InterruptedException ignored) {
      // the background broadcaster will never be interrupted
    }
    this.isClosed.set(true);
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
