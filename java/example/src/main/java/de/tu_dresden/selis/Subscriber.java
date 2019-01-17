package de.tu_dresden.selis;

import de.tu_dresden.selis.pubsub.*;
import de.tu_dresden.selis.pubsub.PubSubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Subscriber implements Runnable {

    final String authHash;

    int quality;

    int price;

    Logger LOG;

    public Subscriber(String authHash, int quality, int price) {
        this.authHash = authHash;
        this.quality = quality;
        this.price = price;
    }

    @Override
    public void run() {
        LOG = LoggerFactory.getLogger("Subscriber[" + Thread.currentThread().getName() + "]");

        try (PubSub c = new PubSub("root.crt", "localhost", 20000)) {
            Subscription subscription = new Subscription(this.authHash);

            //this line can throw exception if we provide value of invalid type. Check ValueType for allowed values
            subscription.add(new Rule("_type", "PKI", RuleType.EQ));
            subscription.add(new Rule("AvgPrice", price, RuleType.GE));
            // subscription.add(new Rule("quality", quality, RuleType.GE)); //this line is equal to the below line:
            // subscription.add(Rule.intRule("quality", quality, RuleType.GE));

            c.subscribe(subscription, new Callback() {
                @Override
                public void onMessage(Message message) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Object> entry : message.entrySet()) {
                        String key = entry.getKey() != null ? entry.getKey() : "";
                        String value = entry.getValue() != null ? entry.getValue().toString() : "";

                        sb.append(key).append("=").append(value).append(", ");
                    }

                    LOG.info("Subscriber[{}], Received {}", authHash, sb.toString());
                }
            });

            LOG.info("Subscriber[{}] looking for AvgPrice >= {}", authHash, price);
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (PubSubException ex) {
            LOG.warn("Could not subscribe, got error: {}", ex.getMessage());
        }

        LOG.info("Finishing");
    }
}
