package de.tu_dresden.selis.pubsub;

public class PubSubException extends RuntimeException {

    public PubSubException() {
    }

    public PubSubException(String message) {
        super(message);
    }

    public PubSubException(String message, Throwable cause) {
        super(message, cause);
    }

    public PubSubException(Throwable cause) {
        super(cause);
    }

    public PubSubException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
