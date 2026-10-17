package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxDomainEventPublisherTest {

    private final OutboxJpaRepository outboxJpaRepository = mock(OutboxJpaRepository.class);
    private final OutboxDomainEventPublisher publisher = new OutboxDomainEventPublisher(
            outboxJpaRepository, new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void savesRaisedEventToOutboxAndClearsAggregateEvents() {
        var order = Order.create(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(1)).getValue();
        order.assign();
        var domainEvent = order.getDomainEvents().getFirst();

        publisher.publish(List.of(order));

        var messageCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxJpaRepository).save(messageCaptor.capture());
        var message = messageCaptor.getValue();
        assertThat(message.getId()).isEqualTo(domainEvent.getEventId());
        assertThat(message.getEventType()).isEqualTo(domainEvent.getClass().getName());
        assertThat(message.getAggregateId()).isEqualTo(order.getId().toString());
        assertThat(message.getPayload()).contains(order.getId().toString());
        assertThat(order.getDomainEvents()).isEmpty();
    }
}
