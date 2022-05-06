package ir.sharif.aic.hideandseek.lib.channel;

public interface Watcher<T> {
  void watch(T msg);
}
