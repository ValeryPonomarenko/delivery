package microarch.delivery.core.domain.model.delivery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void createsLocationAtBoardBoundaries() {
        Location minimum = Location.mustCreate(1, 1);
        Location maximum = Location.mustCreate(10, 10);

        assertThat(minimum.getX()).isEqualTo(1);
        assertThat(minimum.getY()).isEqualTo(1);
        assertThat(maximum.getX()).isEqualTo(10);
        assertThat(maximum.getY()).isEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("outOfRangeCoordinates")
    void rejectsCoordinatesOutsideBoard(int x, int y) {
        assertThat(Location.create(x, y)).satisfies(result -> {
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.out.of.range");
        });
    }

    private static Stream<Arguments> outOfRangeCoordinates() {
        return Stream.of(
                Arguments.of(0, 1),
                Arguments.of(11, 1),
                Arguments.of(1, 0),
                Arguments.of(1, 11));
    }

    @Test
    void locationsWithSameCoordinatesAreEquivalent() {
        Location location = Location.mustCreate(3, 7);
        Location sameLocation = Location.mustCreate(3, 7);

        assertThat(location)
                .isEqualTo(sameLocation)
                .hasSameHashCodeAs(sameLocation);
        assertThat(location.isEquivalentTo(sameLocation)).isTrue();
    }

    @Test
    void locationsDifferingInEitherCoordinateAreNotEquivalent() {
        Location location = Location.mustCreate(3, 7);

        assertThat(location.isEquivalentTo(Location.mustCreate(4, 7))).isFalse();
        assertThat(location.isEquivalentTo(Location.mustCreate(3, 8))).isFalse();
    }

    @Test
    void calculatesDistanceForCourierAndOrderFromBusinessExample() {
        Location courier = Location.mustCreate(4, 9);
        Location order = Location.mustCreate(2, 6);

        assertThat(courier.distanceTo(order)).isEqualTo(5);
        assertThat(order.distanceTo(courier)).isEqualTo(5);
        assertThat(courier.distanceTo(courier)).isZero();
    }
}
