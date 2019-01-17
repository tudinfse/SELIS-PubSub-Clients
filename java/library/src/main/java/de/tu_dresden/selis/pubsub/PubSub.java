package de.tu_dresden.selis.pubsub;

import de.tu_dresden.selis.pubsub.impl.PublisherChannel;
import de.tu_dresden.selis.pubsub.impl.SubscriberChannel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

/**
 * Manages all connections to the SELIS Publish/Subscribe system. Allows to publish messages and register new
 * subscriptions.
 */
public final class PubSub implements AutoCloseable {

    public static final int DEFAULT_PORT = 20000;

    // protected scope - for testing
    protected PublisherChannel publisherChannel;

    // protected scope - for testing
    protected SubscriberChannel subscriberChannel;

    /**
     * Create connection to the SELIS Publish/Subscribe system running on hostname on the default port
     *
     * @param certificateFile - the file with CA certificate which signed the PubSub certificate
     * @param hostname - the valid URL of the SELIS Publish/Subscribe system. Can be IP or hostname
     * @throws PubSubArgumentException thrown if the hostname is an invalid URL address
     * @throws PubSubConnectionException thrown if there is no connection the the Pub/Sub system
     */
    public PubSub(String certificateFile, String hostname) throws PubSubArgumentException, PubSubConnectionException {
        this(certificateFile, hostname, DEFAULT_PORT);
    }

    /**
     * Create connection to the SELIS Publish/Subscribe system running on hostname:portNumber
     *
     * @param certificateFile - the file with CA certificate which signed the PubSub certificate
     * @param hostname - the valid URL of the SELIS Publish/Subscribe system. Can be IP or hostname
     * @param portNumber - TCP port on which Pub/Sub exposes its REST API. Typically 20000
     * @throws PubSubArgumentException thrown if the hostname is an invalid URL address
     * @throws PubSubConnectionException thrown if there is no connection the the Pub/Sub system
     */
    public PubSub(String certificateFile, String hostname, int portNumber) throws PubSubArgumentException, PubSubConnectionException {
        this(PubSub.readCertificate(certificateFile), hostname, portNumber);
    }

    /**
     * Create connection to the SELIS Publish/Subscribe system running on hostname on the default port
     *
     * @param certificate - the instance of X509Certificate which is a CA certificate used to signed the PubSub certificate
     * @param hostname - the valid URL of the SELIS Publish/Subscribe system. Can be IP or hostname
     * @throws PubSubArgumentException thrown if the hostname is an invalid URL address
     * @throws PubSubConnectionException thrown if there is no connection the the Pub/Sub system
     */
    public PubSub(X509Certificate certificate, String hostname) throws PubSubArgumentException, PubSubConnectionException {
        this(certificate, hostname, DEFAULT_PORT);
    }

    /**
     * Create connection to the SELIS Publish/Subscribe system running on hostname:portNumber
     *
     * @param certificate - the instance of X509Certificate which is a CA certificate used to signed the PubSub certificate
     * @param hostname - the valid URL of the SELIS Publish/Subscribe system. Can be IP or hostname
     * @param portNumber - TCP port on which Pub/Sub exposes its REST API. Typically 20000
     * @throws PubSubArgumentException thrown if the hostname is an invalid URL address
     * @throws PubSubConnectionException thrown if there is no connection the the Pub/Sub system
     */
    public PubSub(X509Certificate certificate, String hostname, int portNumber) throws PubSubArgumentException, PubSubConnectionException {
        this.publisherChannel = new PublisherChannel(certificate, hostname, portNumber);
        this.subscriberChannel = new SubscriberChannel(certificate, hostname, portNumber);
    }

    /**
     * Publish new message to the SELIS Publish/Subscribe system.
     *
     * @param message - the message containing key:value pairs which will be send to the SELIS Publish/Subscribe system
     * @throws PubSubException - thrown if the message can not be published due to connection or authentication problems
     */
    public void publish(Message message) throws PubSubException {
        publisherChannel.publish(message);
    }

    /**
     * Subscribe for messages matching criteria specified in Subscription object. The matching messages will be then
     * pushed to the onMessage() method of the given callback object.
     *
     * @param subscription - contains the definition of Subscription, including Authentication data and matching criteria
     * @param callback - the object implementing Callback interface, to which the matching messages are pushed from the
     *                 SELIS Publish/Subscribe system
     * @throws PubSubException - thrown if the message can not be published due to connection or authentication problems
     */
    public void subscribe(Subscription subscription, Callback callback) throws PubSubException {
        subscriberChannel.subscribe(subscription, callback);
    }

    private static X509Certificate readCertificate(String certificateFile) throws PubSubArgumentException {
        String certificate;
        try {
            certificate = new String(readAllBytes(get(certificateFile)));
        } catch (IOException e) {
            throw new PubSubArgumentException("Could not read certificate from " + certificateFile);
        }
        InputStream stream = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
        X509Certificate x509;
        try {
            x509 = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(stream);
        } catch (CertificateException e) {
            throw new PubSubArgumentException("File " + certificateFile + " does not contain valid X509 pem-encoded certificate");
        }

        return x509;
    }

    /**
     * Closes the connection to the SELIS Publish/Subscribe system, cleaning all allocated resources.
     */
    @Override
    public void close() {
        publisherChannel.close();
        subscriberChannel.close();
    }
}
