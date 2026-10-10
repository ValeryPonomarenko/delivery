package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.ports.TransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class SpringTransactionManager implements TransactionManager {

    @Override
    @Transactional
    public void execute(Runnable action) {
        Objects.requireNonNull(action, "action must not be null").run();
    }
}
