package de.tu_dresden.selis.pubsub.impl;

import com.google.gson.Gson;
import de.tu_dresden.selis.pubsub.PubSubArgumentException;
import de.tu_dresden.selis.pubsub.PubSubConnectionException;
import de.tu_dresden.selis.pubsub.PubSubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public abstract class Channel {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String HTTP_METHOD_POST = "POST";

    protected String serverEndpoint;

    protected Gson gson = new Gson();

    protected X509Certificate certificate;

    protected SSLContext sslContext;

    private Logger LOG;

    public Channel(X509Certificate certificate, String hostname, int portNumber) throws PubSubException {
        LOG = LoggerFactory.getLogger(Channel.class);
        this.serverEndpoint = "https://" + hostname + ":" + portNumber;
        this.certificate = certificate;
        try {
            this.sslContext = Channel.getSSLContext(this.certificate);
        } catch (Exception e) {
            throw new PubSubException("Could not initialize SSL context to " + this.serverEndpoint);
        }
    }

    protected <T> T post(String path, Object message) throws PubSubException {
        return post(path, message, null);
    }

    protected <T> T post(String path, Object message, Class<T> clazz) throws PubSubException {
        HttpsURLConnection connection = null;
        OutputStream connectionOutputStream = null;
        BufferedReader connectionReader = null;

        URL url = getUrl(path);

        String json = gson.toJson(message);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending message: {} as JSON to Endpoint: {}", json, url);
        }

        StringBuilder output = new StringBuilder();
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(this.sslContext.getSocketFactory());
            connection.setDoOutput(true);
            connection.setRequestMethod(HTTP_METHOD_POST);
            connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);

            connectionOutputStream = connection.getOutputStream();
            connectionOutputStream.write(json.getBytes());
            connectionOutputStream.flush();

            if (connection.getResponseCode() < 200 || connection.getResponseCode() > 299) {
                throw new PubSubConnectionException("Server " + url + " return error. HTTP error code: " + connection.getResponseCode() + ", message: " + connection.getResponseMessage());
            }

            connectionReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String line;
            while ((line = connectionReader.readLine()) != null) {
                output.append(line);
            }
        } catch (IOException e) {
            throw new PubSubConnectionException(e);
        } finally {
            if (connectionOutputStream != null) {
                try {
                    connectionOutputStream.close();
                } catch (IOException e) {
                    LOG.warn("Could not close output stream to the server connection: " + url);
                }
            }

            if (connectionReader != null) {
                try {
                    connectionReader.close();
                } catch (IOException e) {
                    LOG.warn("Could not close input stream to the server connection: " + url);
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        String jsonOutput = output.toString();
        if (clazz != null && !jsonOutput.isEmpty()) {
            return gson.fromJson(jsonOutput, clazz);
        }

        return null;
    }

    protected static SSLContext getSSLContext(X509Certificate certificate) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry(Integer.toString(1), certificate);

        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(keyStore);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        return context;
    }

    private URL getUrl(String path) {
        String endpoint = serverEndpoint + path;
        try {
            return new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new PubSubArgumentException("Pub/Sub address is not a valid URL: " + endpoint);
        }
    }

    public void close() {
    }

}
