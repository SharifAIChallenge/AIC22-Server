package ir.sharif.aic.hideandseek.lib.channel;

public interface Channel<T> {
  /**
   * push a message through channel so all watchers are notified.
   *
   * @param msg the message to send to all watchers.
   */
  void push(T msg);

  /**
   * process a message without queue.
   * use this method with caution, because it can not handle deadlock.
   *
   * @param msg the message to send to all watchers.
   */
  void process(T msg);

  /**
   * add a watcher to notify later on.
   *
   * @param watcher the watcher to add.
   */
  void addWatcher(Watcher<T> watcher);

  /** close this channel so that watchers can no longer receive messages from it. */
  void close();
}
