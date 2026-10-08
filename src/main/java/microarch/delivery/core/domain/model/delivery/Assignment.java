package microarch.delivery.core.domain.model.delivery;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.Guard;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Assignment extends BaseEntity<UUID> {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "volume", nullable = false))
    private Volume volume;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "location_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "location_y", nullable = false))
    })
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status;

    public Assignment(UUID orderId, Volume volume, Location location) {
        super(UUID.randomUUID());

        Error.throwIf(Guard.againstNullOrEmpty(orderId, "orderId"));

        this.orderId = orderId;
        this.volume = Objects.requireNonNull(volume, "volume must not be null");
        this.location = Objects.requireNonNull(location, "location must not be null");
        this.status = AssignmentStatus.ASSIGNED;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Volume getVolume() {
        return volume;
    }

    public Location getLocation() {
        return location;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void complete(Location courierLocation) {
        Objects.requireNonNull(courierLocation, "courier location must not be null");

        if (status != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException("Only assigned deliveries can be completed");
        }
        if (!location.isWithinOneStepOf(courierLocation)) {
            throw new IllegalStateException("Courier is too far away to complete this delivery");
        }

        status = AssignmentStatus.COMPLETED;
    }
}
