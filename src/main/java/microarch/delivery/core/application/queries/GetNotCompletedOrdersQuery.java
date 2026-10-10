package microarch.delivery.core.application.queries;

public final class GetNotCompletedOrdersQuery {

    private GetNotCompletedOrdersQuery() {
    }

    public static GetNotCompletedOrdersQuery create() {
        return new GetNotCompletedOrdersQuery();
    }
}
