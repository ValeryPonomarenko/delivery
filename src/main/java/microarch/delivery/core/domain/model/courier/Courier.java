package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.delivery.Assignment;
import microarch.delivery.core.domain.model.delivery.AssignmentStatus;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "couriers")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Courier extends Aggregate<UUID> {

    public static final Volume MAX_VOLUME = Volume.mustCreate(20);

    @Column(name = "name", nullable = false)
    private final String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "location_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "location_y", nullable = false))
    })
    private Location location;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "max_volume", nullable = false))
    private final Volume maxVolume;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id", nullable = false)
    private final List<Assignment> assignments = new ArrayList<>();

    private Courier(String name, Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.location = location;
        this.maxVolume = MAX_VOLUME;
    }

    public static Result<Courier, Error> create(String name, Location location) {
        Error error = Guard.combine(
                Guard.againstNullOrEmpty(name, "name"),
                location == null ? GeneralErrors.valueIsRequired("location") : null);
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Courier(name, location));
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Volume getMaxVolume() {
        return maxVolume;
    }

    public List<Assignment> getAssignments() {
        return List.copyOf(assignments);
    }

    public boolean canTakeOrder(Volume orderVolume) {
        Objects.requireNonNull(orderVolume, "order volume must not be null");
        return currentVolume() + orderVolume.getValue() <= maxVolume.getValue();
    }

    public Result<Assignment, Error> takeOrder(Order order) {
        if (order == null) {
            return Result.failure(GeneralErrors.valueIsRequired("order"));
        }

        if (!canTakeOrder(order.getVolume())) {
            return Result.failure(Error.of("courier.capacity.exceeded", "Courier capacity would be exceeded"));
        }
        if (hasAssignmentFor(order.getId())) {
            return Result.failure(Error.of("order.already.assigned", "Order is already assigned to this courier"));
        }

        var assignmentResult = Assignment.create(order.getId(), order.getVolume(), order.getLocation());
        if (assignmentResult.isFailure()) {
            return Result.failure(assignmentResult.getError());
        }
        assignments.add(assignmentResult.getValue());
        return assignmentResult;
    }

    public UnitResult<Error> completeAssignment(UUID assignmentId) {
        Error error = Guard.againstNullOrEmpty(assignmentId, "assignmentId");
        if (error != null) {
            return UnitResult.failure(error);
        }

        Assignment assignment = assignments.stream()
                .filter(candidate -> candidate.getId().equals(assignmentId))
                .findFirst()
                .orElse(null);
        if (assignment == null) {
            return UnitResult.failure(Error.of("assignment.not.found", "Assignment was not found"));
        }
        return assignment.complete(location);
    }

    public UnitResult<Error> completeAssignmentForOrder(UUID orderId) {
        Error error = Guard.againstNullOrEmpty(orderId, "orderId");
        if (error != null) {
            return UnitResult.failure(error);
        }

        Assignment assignment = assignments.stream()
                .filter(candidate -> candidate.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
        if (assignment == null) {
            return UnitResult.failure(Error.of("assignment.not.found", "Assignment was not found"));
        }
        return assignment.complete(location);
    }

    public UnitResult<Error> moveTo(Location newLocation) {
        if (newLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("location"));
        }
        if (!location.isWithinOneStepOf(newLocation)) {
            return UnitResult.failure(Error.of(
                    "courier.location.cannot.be.changed",
                    "Courier can move no more than one step at a time"));
        }
        location = newLocation;
        return UnitResult.success();
    }

    private int currentVolume() {
        return assignments.stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED)
                .mapToInt(assignment -> assignment.getVolume().getValue())
                .sum();
    }

    private boolean hasAssignmentFor(UUID orderId) {
        return assignments.stream().anyMatch(assignment -> assignment.getOrderId().equals(orderId));
    }
}
