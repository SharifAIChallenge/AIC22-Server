package ir.sharif.aic.hideandseek.channel;

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
      for (Watcher<T> watcher : watchers) {
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
        task.start();
      }
    }
  }
}
