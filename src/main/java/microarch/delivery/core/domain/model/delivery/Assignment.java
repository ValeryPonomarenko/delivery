package microarch.delivery.core.domain.model.delivery;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.Guard;

import java.util.Objects;
import java.util.UUID;

public class Assignment extends BaseEntity<UUID> {

    private UUID orderId;
    private Volume volume;
    private Location location;
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
