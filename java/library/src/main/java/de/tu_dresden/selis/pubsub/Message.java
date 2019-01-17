package de.tu_dresden.selis.pubsub;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Message in form of key:pair values, where key is a String and value can be of types defined in ValueType enum.
 */
public class Message extends HashMap<String, Object> {

    public Message(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Message(int initialCapacity) {
        super(initialCapacity);
    }

    public Message() {
    }

    @Override
    public Object put(String key, Object value) {
        //if (!hasValidType(value)) {
        //    throw new PubSubArgumentException("Value of class: " + value.getClass() + " not allowed in the message");
        //}
        return super.put(key, value);
    }

    protected boolean hasValidType(Object value) {
        if (value != null) {
            if (ValueType.byObjectType(value.getClass()) == null) {
                return false;
            }
        }

        return true;
    }
}
