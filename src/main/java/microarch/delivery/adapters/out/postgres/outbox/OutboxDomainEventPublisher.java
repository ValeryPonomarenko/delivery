package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.Aggregate;
import libs.ddd.AggregateRoot;
import libs.ddd.DomainEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;

    public OutboxDomainEventPublisher(OutboxJpaRepository outboxJpaRepository, ObjectMapper objectMapper) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(Iterable<? extends Aggregate<?>> aggregates) {
        try {
            for (AggregateRoot<?> aggregate : aggregates) {
                aggregate.getDomainEvents().forEach(event -> {
                    try {
                        var payload = objectMapper.writeValueAsString(event);
                        var outboxMessage = new OutboxMessage(
                                event.getEventId(),
                                event.getClass().getName(),
                                aggregate.getId().toString(),
                                aggregate.getClass().getSimpleName(),
                                payload,
                                event.getOccurredOnUtc());
                        outboxJpaRepository.save(outboxMessage);
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to serialize domain event for Outbox", exception);
                    }
                });
                aggregate.clearDomainEvents();
            }
        } catch (Exception exception) {
            throw new RuntimeException("Persist events is failed", exception);
        }
    }
}
