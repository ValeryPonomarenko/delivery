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
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
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

    private Assignment(UUID orderId, Volume volume, Location location) {
        super(UUID.randomUUID());
        this.orderId = orderId;
        this.volume = Objects.requireNonNull(volume, "volume must not be null");
        this.location = Objects.requireNonNull(location, "location must not be null");
        this.status = AssignmentStatus.ASSIGNED;
    }

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {
        Error error = Guard.combine(
                Guard.againstNullOrEmpty(orderId, "orderId"),
                volume == null ? GeneralErrors.valueIsRequired("volume") : null,
                location == null ? GeneralErrors.valueIsRequired("location") : null);
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(orderId, volume, location));
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

    public UnitResult<Error> complete(Location courierLocation) {
        if (courierLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("courierLocation"));
        }

        if (status != AssignmentStatus.ASSIGNED) {
            return UnitResult.failure(Errors.assignmentCannotBeCompleted());
        }
        if (!location.isWithinOneStepOf(courierLocation)) {
            return UnitResult.failure(Errors.courierIsTooFarAway());
        }

        status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

    private static final class Errors {
        static Error assignmentCannotBeCompleted() {
            return Error.of("assignment.cannot.be.completed", "Only assigned deliveries can be completed");
        }

        static Error courierIsTooFarAway() {
            return Error.of("courier.is.too.far.away", "Courier is too far away to complete this delivery");
        }
    }
}
