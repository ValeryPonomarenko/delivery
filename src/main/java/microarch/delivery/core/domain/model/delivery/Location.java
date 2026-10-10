package microarch.delivery.core.domain.model.delivery;

import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Location extends ValueObject<Location> {

    public static final int MIN_COORDINATE = 1;
    public static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    private Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Result<Location, Error> create(int x, int y) {
        Error error = Guard.combine(
                Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x"),
                Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y"));
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Location(x, y));
    }

    public static Location mustCreate(int x, int y) {
        return create(x, y).getValueOrThrow();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int distanceTo(Location other) {
        Objects.requireNonNull(other, "other location must not be null");
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    public boolean isWithinOneStepOf(Location other) {
        return distanceTo(other) <= 1;
    }

    public boolean isEquivalentTo(Location other) {
        return equals(other);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
