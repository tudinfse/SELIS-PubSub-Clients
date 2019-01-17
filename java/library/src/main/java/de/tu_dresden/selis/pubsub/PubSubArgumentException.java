package de.tu_dresden.selis.pubsub;

public class PubSubArgumentException extends PubSubException {

    public PubSubArgumentException() {
    }

    public PubSubArgumentException(String message) {
        super(message);
    }

    public PubSubArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public PubSubArgumentException(Throwable cause) {
        super(cause);
    }

    public PubSubArgumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
