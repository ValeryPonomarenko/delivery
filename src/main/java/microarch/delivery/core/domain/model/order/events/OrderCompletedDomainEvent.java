package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class OrderCompletedDomainEvent extends DomainEvent {

    private final UUID orderId;

    public OrderCompletedDomainEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
