package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;

import java.util.Objects;
import java.util.UUID;

public class Order extends Aggregate<UUID> {

    private final Location location;
    private final Volume volume;
    private OrderStatus status;

    public Order(UUID id, Location location, Volume volume) {
        super(id);
        Error.throwIf(Guard.againstNullOrEmpty(id, "orderId"));

        this.location = Objects.requireNonNull(location, "location must not be null");
        this.volume = Objects.requireNonNull(volume, "volume must not be null");
        this.status = OrderStatus.CREATED;
    }

    public Location getLocation() {
        return location;
    }

    public Volume getVolume() {
        return volume;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void assign() {
        changeStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED);
    }

    public void complete() {
        changeStatus(OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
    }

    private void changeStatus(OrderStatus expectedStatus, OrderStatus newStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                    "Order status must be " + expectedStatus + " before changing it to " + newStatus);
        }
        status = newStatus;
    }
}
