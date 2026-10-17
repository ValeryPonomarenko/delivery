package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class OrderCompletedDomainEvent extends DomainEvent {

    private final UUID orderId;

    public OrderCompletedDomainEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
