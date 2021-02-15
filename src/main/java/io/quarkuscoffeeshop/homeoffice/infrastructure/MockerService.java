package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;

@QuarkusMain
@Singleton
public class MockerService  implements QuarkusApplication {

    final Logger logger = LoggerFactory.getLogger(MockerService.class);

    @Inject
    OrderMocker orderMocker;

    private boolean running = true;
    public boolean pause = false;

    @Override
    public int run(String... args) throws Exception {
        logger.info("starting");
        mock.run();
        return 10;
    }

    private Runnable mock = () -> {

        while (running == true) {
            try {
                while (!pause){
                    Thread.sleep(1000);
                }
                Thread.sleep((new Random().nextInt(3)+1) * 1000);
                orderMocker.mockAndPersistOrder();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
