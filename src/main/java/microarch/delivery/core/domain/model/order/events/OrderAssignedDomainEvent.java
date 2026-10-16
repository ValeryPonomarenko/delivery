package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class OrderAssignedDomainEvent extends DomainEvent {

    private final UUID orderId;

    public OrderAssignedDomainEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
