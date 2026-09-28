package microarch.delivery.core.domain.model.delivery;

import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentTest {

    @Test
    void createsAssignedDeliveryTask() {
        UUID orderId = UUID.randomUUID();
        Volume volume = new Volume(3);
        Location location = new Location(2, 6);

        Assignment assignment = new Assignment(orderId, volume, location);

        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(location);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void generatesUniqueIdentityForEveryAssignment() {
        Assignment first = new Assignment(UUID.randomUUID(), new Volume(1), new Location(1, 1));
        Assignment second = new Assignment(UUID.randomUUID(), new Volume(2), new Location(2, 2));

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void completesWhenCourierIsOneStepFromOrder() {
        Assignment assignment = new Assignment(UUID.randomUUID(), new Volume(1), new Location(2, 6));

        assignment.complete(new Location(3, 6));

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void doesNotCompleteWhenCourierIsMoreThanOneStepFromOrder() {
        Assignment assignment = new Assignment(UUID.randomUUID(), new Volume(1), new Location(2, 6));

        assertThatThrownBy(() -> assignment.complete(new Location(4, 9)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void rejectsMissingAssignmentFieldsAndNonPositiveVolume() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> new Assignment(null, new Volume(1), new Location(1, 1)))
                .isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new Assignment(orderId, null, new Location(1, 1)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Volume(0)).isInstanceOf(DomainInvariantException.class);
    }
}
