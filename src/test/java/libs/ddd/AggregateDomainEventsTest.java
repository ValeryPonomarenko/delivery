package libs.ddd;

import org.junit.jupiter.api.Test;
import microarch.delivery.core.ports.DomainEventProducer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AggregateDomainEventsTest {

    @Test
    void storesAndClearsRaisedDomainEvents() {
        var aggregate = new TestAggregate();
        var event = new TestDomainEvent();

        aggregate.raiseDomainEvent(event);

        assertThat(aggregate.getDomainEvents()).containsExactly(event);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredOnUtc()).isBeforeOrEqualTo(Instant.now());

        aggregate.clearDomainEvents();

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }

    @Test
    void publishesAndClearsEventsFromAggregates() {
        var domainEventProducer = mock(DomainEventProducer.class);
        var domainEventPublisher = new microarch.delivery.DefaultDomainEventPublisher(domainEventProducer);
        var firstAggregate = new TestAggregate();
        var secondAggregate = new TestAggregate();
        var firstEvent = new TestDomainEvent();
        var secondEvent = new TestDomainEvent();
        firstAggregate.raiseDomainEvent(firstEvent);
        secondAggregate.raiseDomainEvent(secondEvent);

        domainEventPublisher.publish(List.of(firstAggregate, secondAggregate));

        verify(domainEventProducer).produce(firstEvent);
        verify(domainEventProducer).produce(secondEvent);
        assertThat(firstAggregate.getDomainEvents()).isEmpty();
        assertThat(secondAggregate.getDomainEvents()).isEmpty();
    }

    private static final class TestAggregate extends Aggregate<UUID> {

        private TestAggregate() {
            super(UUID.randomUUID());
        }
    }

    private static final class TestDomainEvent extends DomainEvent {
    }
}
