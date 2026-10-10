package microarch.delivery.core.ports;

public interface TransactionManager {

    void execute(Runnable action);
}
