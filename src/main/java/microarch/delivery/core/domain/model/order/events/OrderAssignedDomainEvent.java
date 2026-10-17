package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class OrderAssignedDomainEvent extends DomainEvent {

    private final UUID orderId;

    public OrderAssignedDomainEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
