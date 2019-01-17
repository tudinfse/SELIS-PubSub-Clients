package de.tu_dresden.selis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class App {
    public static void main(String[] args) throws InterruptedException {
        Logger LOG = LoggerFactory.getLogger(App.class);
        LOG.info("Starting Pub/Sub simulation");

        Thread subscriberA = new Thread(new Subscriber("A", 30, 100), "subscriber1");
        subscriberA.start();

        Thread subscriberB = new Thread(new Subscriber("B", 30, 100), "subscriber2");
        subscriberB.start();

        Thread publisher1 = new Thread(new Publisher(150, "PL"), "publisher1");
        publisher1.start();

        Thread publisher2 = new Thread(new Publisher(3000, "DE"), "publisher2");
        publisher2.start();

        Thread publisher3 = new Thread(new Publisher(800, "PL"), "publisher3");
        publisher3.start();

        publisher1.join();
        publisher2.join();
        publisher3.join();
        subscriberA.interrupt();
        subscriberB.interrupt();
    }
}
