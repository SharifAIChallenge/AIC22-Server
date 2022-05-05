package ir.sharif.aic.hideandseek.channel;

public interface Watcher<T> {
  void watch(T msg);
}
