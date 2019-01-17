package de.tu_dresden.selis.pubsub.impl;

import com.google.gson.Gson;
import de.tu_dresden.selis.pubsub.Callback;
import de.tu_dresden.selis.pubsub.Message;
import de.tu_dresden.selis.pubsub.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SubscriptionListener implements Runnable {

    private final Subscription subscription;

    private final SSLContext sslContext;

    private final String hostname;

    private final int port;

    private final Callback callback;

    private final Logger LOG;

    private AtomicBoolean closeSocket = new AtomicBoolean(false);

    private BufferedReader streamReader = null;
    private PrintWriter streamWriter = null;
    private SSLSocket socket = null;

    public SubscriptionListener(SSLContext sslContext, String host, int port, Subscription subscription, Callback callback) {
        LOG = LoggerFactory.getLogger("SubscriptionListener[subscriptionId=" + subscription.getSubscriptionId() + "]");
        this.subscription = subscription;
        this.sslContext = sslContext;
        this.hostname = host;
        this.port = port;
        this.callback = callback;
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Connecting to Subscription socket on: {}:{}", hostname, port);
        }

        try {
            SSLSocketFactory ssf = this.sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) ssf.createSocket(hostname, port);
            socket.startHandshake();

//            socket = new Socket();
//            socket.connect(new InetSocketAddress(hostname, port));

            streamWriter = new PrintWriter(socket.getOutputStream());
            streamReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending authorization data: {}", this.convertToJson(subscription));
            }
            streamWriter.append(this.convertToJson(subscription)).append('\n').flush();

            while (closeSocket.get() == false) {
                String line = streamReader.readLine();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Received line: {}", line);
                }

                Message message = new Gson().fromJson(line, Message.class);
                if (message != null) {
                    callback.onMessage(message);
                }
            }
        } catch (Exception ex) {
            // io Streams are non-interruptible which means, the read()/write() operations
            // will block whole Thread and it will be impossible to stop it even with
            // the Interrupt call. Therefore, the manager of this class, keeps the reference to it
            // and once it decides that the Thread has to be stopped, it calls close() which closes the stream
            // causing the read()/write() operations to throw an IOException. As this is expected situation
            // we detect that the Thread is supposed to be stopped (closeSocket == true) and we ignore that Exception
            if (closeSocket.get() == true) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Listener thread interrupted");
                }
            } else {
                LOG.warn("Listener exception", ex);
            }
        } finally {
            close();
        }
    }

    public void close() {
        if (closeSocket.get() == true) {
            return;
        }
        closeSocket.set(true);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing Socket connection");
        }

        if (streamWriter != null) {
            streamWriter.close();
        }

        if (streamReader != null) {
            try {
                streamReader.close();
            } catch (IOException ex) {
                LOG.warn("Could not close input stream", ex);
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                LOG.warn("Could not close socket", ex);
            }
        }
    }

    protected String convertToJson(Subscription subscription) {
        Map authMap = new HashMap();
        authMap.put("authHash", subscription.getAuthenticationHash());
        authMap.put("subscriptionId", subscription.getSubscriptionId());

        return new Gson().toJson(authMap);
    }
}
