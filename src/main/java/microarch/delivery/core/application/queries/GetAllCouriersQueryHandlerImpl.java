package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    private final CourierRepository courierRepository;

    public GetAllCouriersQueryHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public Result<GetAllCouriersResponse, Error> handle(GetAllCouriersQuery query) {
        var couriers = courierRepository.findAll().stream()
                .map(courier -> new CourierDto(
                        courier.getId(),
                        courier.getName(),
                        new LocationDto(courier.getLocation().getX(), courier.getLocation().getY())))
                .toList();
        return Result.success(new GetAllCouriersResponse(couriers));
    }
}
