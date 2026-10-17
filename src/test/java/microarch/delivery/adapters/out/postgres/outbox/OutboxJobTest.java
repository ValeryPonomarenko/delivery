package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxJobTest {

    private final DomainEventProducer domainEventProducer = mock(DomainEventProducer.class);
    private final OutboxJpaRepository outboxJpaRepository = mock(OutboxJpaRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final OutboxJob outboxJob = new OutboxJob(domainEventProducer, outboxJpaRepository, objectMapper);

    @Test
    void sendsUnprocessedEventAndMarksMessageAsProcessed() throws Exception {
        var event = new OrderCompletedDomainEvent(UUID.randomUUID());
        var message = new OutboxMessage(
                event.getEventId(),
                event.getClass().getName(),
                event.getOrderId().toString(),
                "Order",
                objectMapper.writeValueAsString(event),
                Instant.now());
        when(outboxJpaRepository.findUnprocessedMessages()).thenReturn(List.of(message));

        outboxJob.run();

        var eventCaptor = ArgumentCaptor.forClass(OrderCompletedDomainEvent.class);
        verify(domainEventProducer).produce(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getOrderId()).isEqualTo(event.getOrderId());
        assertThat(message.getProcessedOnUtc()).isNotNull();
        verify(outboxJpaRepository).save(message);
    }

    @Test
    void leavesMessageUnprocessedWhenPayloadIsNotDomainEvent() {
        var message = new OutboxMessage(
                UUID.randomUUID(),
                String.class.getName(),
                UUID.randomUUID().toString(),
                "Order",
                "\"invalid event\"",
                Instant.now());
        when(outboxJpaRepository.findUnprocessedMessages()).thenReturn(List.of(message));

        outboxJob.run();

        verify(domainEventProducer, never()).produce(org.mockito.ArgumentMatchers.any());
        verify(outboxJpaRepository, never()).save(message);
        assertThat(message.getProcessedOnUtc()).isNull();
    }
}
