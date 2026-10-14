package microarch.delivery.adapters.out.kafka;

import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import queues.order.events.OrderEventsProto;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventProducer implements DomainEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${app.kafka.order-events-topic}")
    private String topic;

    @Override
    public void produce(DomainEvent event) {
        try {
            switch (event) {
            case OrderAssignedDomainEvent assignedEvent -> kafkaTemplate.send(
                    topic,
                    assignedEvent.getOrderId().toString(),
                    OrderEventsProto.OrderAssignedIntegrationEvent.newBuilder()
                            .setOrderId(assignedEvent.getOrderId().toString())
                            .build()
                            .toByteArray()).get();
            case OrderCompletedDomainEvent completedEvent -> kafkaTemplate.send(
                    topic,
                    completedEvent.getOrderId().toString(),
                    OrderEventsProto.OrderCompletedIntegrationEvent.newBuilder()
                            .setOrderId(completedEvent.getOrderId().toString())
                            .build()
                            .toByteArray()).get();
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka publish interrupted", exception);
        } catch (ExecutionException exception) {
            throw new RuntimeException("Kafka publish failed", exception);
        }
    }
}
