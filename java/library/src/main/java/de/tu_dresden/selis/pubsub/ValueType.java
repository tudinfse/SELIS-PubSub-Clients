package de.tu_dresden.selis.pubsub;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * Possible type of the values stored in the Message.
 */
public enum ValueType {

    @SerializedName("string")
    STRING(String.class),

    @SerializedName("float")
    FLOAT(Float.class),

    @SerializedName("float")
    DOUBLE(Double.class),

    @SerializedName("int")
    INTEGER(Integer.class),

    @SerializedName("boolean")
    BOOLEAN(Boolean.class);

    private Class clazz;

    ValueType(Class clazz) {
        this.clazz = clazz;
    }

    public static ValueType byObjectType(Class clazz) {
        for (ValueType vt : ValueType.values()) {
            if (vt.clazz.isAssignableFrom(clazz)) {
                return vt;
            }
        }
        return null;
    }

}
