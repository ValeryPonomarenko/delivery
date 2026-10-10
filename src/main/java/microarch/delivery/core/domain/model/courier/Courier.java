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
import libs.errs.Guard;
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

    public static final Volume MAX_VOLUME = new Volume(20);

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

    public Courier(String name, Location location) {
        super(UUID.randomUUID());
        Error.throwIf(Guard.againstNullOrEmpty(name, "name"));

        this.name = name;
        this.location = Objects.requireNonNull(location, "location must not be null");
        this.maxVolume = MAX_VOLUME;
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

    public Assignment takeOrder(Order order) {
        Objects.requireNonNull(order, "order must not be null");

        if (!canTakeOrder(order.getVolume())) {
            throw new IllegalStateException("Courier capacity would be exceeded");
        }
        if (hasAssignmentFor(order.getId())) {
            throw new IllegalStateException("Order is already assigned to this courier");
        }

        Assignment assignment = new Assignment(order.getId(), order.getVolume(), order.getLocation());
        assignments.add(assignment);
        return assignment;
    }

    public void completeAssignment(UUID assignmentId) {
        Error.throwIf(Guard.againstNullOrEmpty(assignmentId, "assignmentId"));

        Assignment assignment = assignments.stream()
                .filter(candidate -> candidate.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Assignment was not found"));
        assignment.complete(location);
    }

    public void completeAssignmentForOrder(UUID orderId) {
        Error.throwIf(Guard.againstNullOrEmpty(orderId, "orderId"));

        Assignment assignment = assignments.stream()
                .filter(candidate -> candidate.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Assignment was not found"));
        assignment.complete(location);
    }

    public void moveTo(Location newLocation) {
        Objects.requireNonNull(newLocation, "new location must not be null");
        if (!location.isWithinOneStepOf(newLocation)) {
            throw new IllegalArgumentException("Courier can move no more than one step at a time");
        }
        location = newLocation;
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
