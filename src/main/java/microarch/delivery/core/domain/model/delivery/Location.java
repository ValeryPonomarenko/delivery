package microarch.delivery.core.domain.model.delivery;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;

import java.util.List;
import java.util.Objects;

public final class Location extends ValueObject<Location> {

    public static final int MIN_COORDINATE = 1;
    public static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    public Location(int x, int y) {
        Error.throwIf(Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x"));
        Error.throwIf(Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y"));

        this.x = x;
        this.y = y;
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

    public boolean isEquivalentTo(Location other) {
        return equals(other);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
