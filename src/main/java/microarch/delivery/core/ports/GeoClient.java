package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.delivery.Location;

public interface GeoClient {

    Result<Location, Error> getLocation(String street);
}
