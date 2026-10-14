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
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;

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

    private Order(UUID id, Location location, Volume volume) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = OrderStatus.CREATED;
    }

    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Error error = Guard.combine(
                Guard.againstNullOrEmpty(id, "orderId"),
                location == null ? GeneralErrors.valueIsRequired("location") : null,
                volume == null ? GeneralErrors.valueIsRequired("volume") : null);
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Order(id, location, volume));
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

    public UnitResult<Error> assign() {
        var statusChangeResult = changeStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED);
        if (statusChangeResult.isFailure()) {
            return statusChangeResult;
        }

        raiseDomainEvent(new OrderAssignedDomainEvent(getId()));
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        var statusChangeResult = changeStatus(OrderStatus.ASSIGNED, OrderStatus.COMPLETED);
        if (statusChangeResult.isFailure()) {
            return statusChangeResult;
        }

        raiseDomainEvent(new OrderCompletedDomainEvent(getId()));
        return UnitResult.success();
    }

    private UnitResult<Error> changeStatus(OrderStatus expectedStatus, OrderStatus newStatus) {
        if (status != expectedStatus) {
            return UnitResult.failure(Error.of(
                    "order.status.cannot.be.changed",
                    "Order status must be " + expectedStatus + " before changing it to " + newStatus));
        }
        status = newStatus;
        return UnitResult.success();
    }
}
