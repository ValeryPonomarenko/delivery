package microarch.delivery.core.domain.model.delivery;

import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationTest {

    @Test
    void createsLocationAtBoardBoundaries() {
        Location minimum = new Location(1, 1);
        Location maximum = new Location(10, 10);

        assertThat(minimum.getX()).isEqualTo(1);
        assertThat(minimum.getY()).isEqualTo(1);
        assertThat(maximum.getX()).isEqualTo(10);
        assertThat(maximum.getY()).isEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("outOfRangeCoordinates")
    void rejectsCoordinatesOutsideBoard(int x, int y) {
        assertThatThrownBy(() -> new Location(x, y))
                .isInstanceOf(DomainInvariantException.class);
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
        Location location = new Location(3, 7);
        Location sameLocation = new Location(3, 7);

        assertThat(location)
                .isEqualTo(sameLocation)
                .hasSameHashCodeAs(sameLocation);
        assertThat(location.isEquivalentTo(sameLocation)).isTrue();
    }

    @Test
    void locationsDifferingInEitherCoordinateAreNotEquivalent() {
        Location location = new Location(3, 7);

        assertThat(location.isEquivalentTo(new Location(4, 7))).isFalse();
        assertThat(location.isEquivalentTo(new Location(3, 8))).isFalse();
    }

    @Test
    void calculatesDistanceForCourierAndOrderFromBusinessExample() {
        Location courier = new Location(4, 9);
        Location order = new Location(2, 6);

        assertThat(courier.distanceTo(order)).isEqualTo(5);
        assertThat(order.distanceTo(courier)).isEqualTo(5);
        assertThat(courier.distanceTo(courier)).isZero();
    }
}
