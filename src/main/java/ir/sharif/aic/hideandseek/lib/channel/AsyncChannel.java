package ir.sharif.aic.hideandseek.lib.channel;

import java.util.ArrayList;
import java.util.List;

public class AsyncChannel<T> implements Channel<T> {
  private final List<Watcher<T>> watchers;
  private final Object lock;

  public AsyncChannel() {
    this.watchers = new ArrayList<>();
    this.lock = new Object();
  }

  @Override
  public void addWatcher(Watcher<T> watcher) {
    synchronized (lock) {
      this.watchers.add(watcher);
    }
  }

  @Override
  public void push(T msg) {
    synchronized (lock) {
      var tasks = startWatchTasks(msg);
      joinWatchTasks(tasks);
    }
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

  private void joinWatchTasks(List<Thread> tasks) {
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
