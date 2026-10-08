package microarch.delivery.core.domain.model.order;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Order extends Aggregate<UUID> {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "location_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "location_y", nullable = false))
    })
    private final Location location;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "volume", nullable = false))
    private final Volume volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
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
