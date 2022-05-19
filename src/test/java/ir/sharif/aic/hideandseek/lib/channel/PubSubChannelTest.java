package ir.sharif.aic.hideandseek.lib.channel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PubSubChannelTest {
  private static record MockEvent(String message) {}

  @Test
  void testPush_whenGivenAnEventProducerWatcher_thenThereIsNoDeadLock() {
    class EventProducer implements Watcher<MockEvent> {
      public final PubSubChannel<MockEvent> channel;
      public boolean isCalled;

      public EventProducer(PubSubChannel<MockEvent> channel) {
        this.channel = channel;
        this.isCalled = false;
      }

      @Override
      public void watch(MockEvent msg) {
        this.isCalled = true;
        this.channel.push(new MockEvent("secondary event"));
      }
    }

    var channel = new PubSubChannel<MockEvent>();
    var watcher = new EventProducer(channel);
    channel.addWatcher(watcher);
    channel.push(new MockEvent("first event"));
    channel.close();

    assertThat(watcher.isCalled).isTrue();
  }

  @Test
  void testPush_whenGivenANormalWatcher_thenWatcherIsTriggeredOnPush() {
    class NormalProducer implements Watcher<MockEvent> {
      public final MockEvent expectedEvent;
      public boolean isCalled;

      public NormalProducer(MockEvent expectedEvent) {
        this.isCalled = false;
        this.expectedEvent = expectedEvent;
      }

      @Override
      public void watch(MockEvent msg) {
        if (msg.equals(this.expectedEvent)) this.isCalled = true;
      }
    }

    var expectedEvent = new MockEvent("first event");
    var channel = new PubSubChannel<MockEvent>();
    var watcher = new NormalProducer(expectedEvent);
    channel.addWatcher(watcher);
    channel.push(expectedEvent);
    channel.close();

    assertThat(watcher.isCalled).isTrue();
  }
}
