package ir.sharif.aic.hideandseek.lib.channel;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
    class NormalWatcher implements Watcher<MockEvent> {
      public final MockEvent expectedEvent;
      public boolean isCalled;

      public NormalWatcher(MockEvent expectedEvent) {
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
    var watcher = new NormalWatcher(expectedEvent);
    channel.addWatcher(watcher);
    channel.push(expectedEvent);
    channel.close();

    assertThat(watcher.isCalled).isTrue();
  }

  @Test
  void testPush_whenGivenMultipleEventsAndWatchers_thenOrderIsConservedInWatchers() {
    class CollectorWatcher implements Watcher<MockEvent> {
      public final List<MockEvent> collectedEvents;

      public CollectorWatcher() {
        this.collectedEvents = new ArrayList<>();
      }

      @Override
      public void watch(MockEvent msg) {
        this.collectedEvents.add(msg);
      }
    }

    var expectedOrder =
        List.of(
            new MockEvent("1"),
            new MockEvent("2"),
            new MockEvent("3"),
            new MockEvent("4"),
            new MockEvent("5"));

    var channel = new PubSubChannel<MockEvent>();

    // construct some watchers
    var watchers = new ArrayList<CollectorWatcher>();
    for (int i = 0; i < 10; i++) {
      var watcher = new CollectorWatcher();
      watchers.add(watcher);
      channel.addWatcher(watcher);
    }

    expectedOrder.forEach(channel::push);
    channel.close();

    watchers.forEach(watcher -> assertThat(watcher.collectedEvents).isEqualTo(expectedOrder));
  }
}
