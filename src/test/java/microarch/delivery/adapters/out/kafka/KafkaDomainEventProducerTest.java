package microarch.delivery.adapters.out.kafka;

import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import queues.order.events.OrderEventsProto;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class KafkaDomainEventProducerTest {

    private final KafkaTemplate<String, byte[]> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaDomainEventProducer producer = new KafkaDomainEventProducer(kafkaTemplate);

    KafkaDomainEventProducerTest() {
        ReflectionTestUtils.setField(producer, "topic", "order.events");
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void sendsAssignedIntegrationEvent() throws Exception {
        var orderId = UUID.randomUUID();

        producer.produce(new OrderAssignedDomainEvent(orderId));

        var payload = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("order.events"),
                org.mockito.ArgumentMatchers.eq(orderId.toString()), payload.capture());
        var event = OrderEventsProto.OrderAssignedIntegrationEvent.parseFrom(payload.getValue());
        assertThat(event.getOrderId()).isEqualTo(orderId.toString());
    }

    @Test
    void sendsCompletedIntegrationEvent() throws Exception {
        var orderId = UUID.randomUUID();

        producer.produce(new OrderCompletedDomainEvent(orderId));

        var payload = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("order.events"),
                org.mockito.ArgumentMatchers.eq(orderId.toString()), payload.capture());
        var event = OrderEventsProto.OrderCompletedIntegrationEvent.parseFrom(payload.getValue());
        assertThat(event.getOrderId()).isEqualTo(orderId.toString());
    }
}
