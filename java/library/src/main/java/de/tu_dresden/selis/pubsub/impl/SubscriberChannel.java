package de.tu_dresden.selis.pubsub.impl;

import de.tu_dresden.selis.pubsub.Callback;
import de.tu_dresden.selis.pubsub.PubSubException;
import de.tu_dresden.selis.pubsub.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SubscriberChannel extends Channel {

    public static final String SUBSCRIBE_PATH = "/subscribe";
    public static final int THREAD_POOL_SIZE = 20;

    private Logger LOG = LoggerFactory.getLogger(SubscriberChannel.class);

    protected ExecutorService executorService;

    protected List<SubscriptionListener> listeners = new LinkedList<>();

    public SubscriberChannel(final X509Certificate certificate, final String hostname, int portNumber) {
        super(certificate, hostname, portNumber);
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void subscribe(final Subscription subscription, final Callback callback) throws PubSubException {
        SubscriptionSocket response = post(SUBSCRIBE_PATH, subscription, SubscriptionSocket.class);
        subscription.setSubscriptionId(response.subscriptionId);

        SubscriptionListener listener = new SubscriptionListener(this.sslContext, response.host, response.port, subscription, callback);
        listeners.add(listener);
        executorService.execute(listener);
    }

    @Override
    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing Subscriber Channel");
        }
        executorService.shutdown();

        // iterate though all Threads and close them. It is required to explicite
        // call close() on each listener, as they can be blocked in IO read()/write()
        // and standard Interrupt will not stop them
        for (SubscriptionListener listener : listeners) {
            listener.close();
        }

        try {
            if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public final class SubscriptionSocket {
        private String host;

        private int port;

        private String subscriptionId;

        public SubscriptionSocket() {
        }
    }

}
