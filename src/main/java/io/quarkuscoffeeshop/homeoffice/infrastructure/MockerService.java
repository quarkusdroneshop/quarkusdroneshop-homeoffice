package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Random;

@QuarkusMain
public class MockerService  implements QuarkusApplication {

    final Logger logger = LoggerFactory.getLogger(MockerService.class);

    @Inject
    OrderMocker orderMocker;

    private boolean running = true;

    @Override
    public int run(String... args) throws Exception {
        logger.info("starting");
        mock.run();
        return 10;
    }

    private Runnable mock = () -> {

        while (running == true) {
            try {
                Thread.sleep((new Random().nextInt(3)+1) * 1000);
                orderMocker.mockAndPersistOrder();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
