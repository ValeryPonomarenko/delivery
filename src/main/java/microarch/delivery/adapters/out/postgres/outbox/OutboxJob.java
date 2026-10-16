package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxJob {

    private final DomainEventProducer domainEventProducer;
    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1_000)
    public void run() {
        for (var message : outboxJpaRepository.findUnprocessedMessages()) {
            try {
                var eventClass = Class.forName(message.getEventType());
                var eventObject = objectMapper.readValue(message.getPayload(), eventClass);
                if (!(eventObject instanceof libs.ddd.DomainEvent domainEvent)) {
                    throw new IllegalStateException("Invalid outbox message type: " + eventClass);
                }

                domainEventProducer.produce(domainEvent);
                message.markAsProcessed();
                outboxJpaRepository.save(message);
            } catch (Exception exception) {
                log.error("Failed to publish outbox message {}", message.getId(), exception);
            }
        }
    }
}
