package de.tu_dresden.selis;


import de.tu_dresden.selis.pubsub.PubSub;
import de.tu_dresden.selis.pubsub.Message;
import de.tu_dresden.selis.pubsub.PubSubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class Publisher implements Runnable {

    Logger LOG;

    Random rand = new Random();

    String country;

    int maxPrice;

    public Publisher(int maxPrice, String country) {
        this.maxPrice = maxPrice;
        this.country = country;
    }

    @Override
    public void run() {
        LOG = LoggerFactory.getLogger("Publisher[" + Thread.currentThread().getName() + "]");
        LOG.info("Starting");

        try (PubSub c = new PubSub("root.crt", "localhost", 20000)) {
            for (int i = 0; i < 10; i++) {
                Message msg = new Message();
                msg.put("_type", "PKI");
                msg.put("Country", country);
                msg.put("OrderId", UUID.randomUUID().toString());
                msg.put("Lat", 52);
                msg.put("Lon", 28);
                msg.put("AvgPrice", rand.nextInt(maxPrice) + 1);
                msg.put("Quality", rand.nextInt(100));
                c.publish(msg);

                LOG.info("Published orderId={}, AvgPrice={}, Country={}, Quality={}", msg.get("OrderId"), msg.get("AvgPrice"), msg.get("Country"), msg.get("quality"));
                try {
                    Thread.sleep(rand.nextInt(1500) + 100);
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted");
                }
            }
        } catch (PubSubException ex) {
            LOG.warn("Could not publish messages, got exception: {}", ex.getMessage());
        }

        LOG.info("Finishing");
    }
}
