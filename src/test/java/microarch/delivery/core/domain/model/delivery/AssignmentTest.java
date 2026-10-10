package microarch.delivery.core.domain.model.delivery;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentTest {

    @Test
    void createsAssignedDeliveryTask() {
        UUID orderId = UUID.randomUUID();
        Volume volume = Volume.mustCreate(3);
        Location location = Location.mustCreate(2, 6);

        Assignment assignment = Assignment.create(orderId, volume, location).getValueOrThrow();

        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(location);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void generatesUniqueIdentityForEveryAssignment() {
        Assignment first = Assignment.create(UUID.randomUUID(), Volume.mustCreate(1), Location.mustCreate(1, 1)).getValueOrThrow();
        Assignment second = Assignment.create(UUID.randomUUID(), Volume.mustCreate(2), Location.mustCreate(2, 2)).getValueOrThrow();

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void completesWhenCourierIsOneStepFromOrder() {
        Assignment assignment = Assignment.create(UUID.randomUUID(), Volume.mustCreate(1), Location.mustCreate(2, 6)).getValueOrThrow();

        assertThat(assignment.complete(Location.mustCreate(3, 6)).isSuccess()).isTrue();

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void doesNotCompleteWhenCourierIsMoreThanOneStepFromOrder() {
        Assignment assignment = Assignment.create(UUID.randomUUID(), Volume.mustCreate(1), Location.mustCreate(2, 6)).getValueOrThrow();

        var result = assignment.complete(Location.mustCreate(4, 9));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.is.too.far.away");
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void rejectsMissingAssignmentFieldsAndNonPositiveVolume() {
        UUID orderId = UUID.randomUUID();

        assertThat(Assignment.create(null, Volume.mustCreate(1), Location.mustCreate(1, 1)).isFailure()).isTrue();
        assertThat(Assignment.create(orderId, null, Location.mustCreate(1, 1)).isFailure()).isTrue();
        assertThat(Volume.create(0).isFailure()).isTrue();
    }
}
