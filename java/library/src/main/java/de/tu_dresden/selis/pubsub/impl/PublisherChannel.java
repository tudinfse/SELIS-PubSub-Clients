package de.tu_dresden.selis.pubsub.impl;

import de.tu_dresden.selis.pubsub.Message;
import de.tu_dresden.selis.pubsub.PubSubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

public final class PublisherChannel extends Channel {

    private static final String PUBLISH_PATH = "/publish";

    private Logger LOG = LoggerFactory.getLogger(PublisherChannel.class);

    public PublisherChannel(final X509Certificate certificate, final String hostname, int portNumber) {
        super(certificate, hostname, portNumber);
    }

    public void publish(final Message message) throws PubSubException {
        post(PUBLISH_PATH, message);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Message {} published to endpoint={}{}", message, serverEndpoint, PUBLISH_PATH);
        }
    }
}
