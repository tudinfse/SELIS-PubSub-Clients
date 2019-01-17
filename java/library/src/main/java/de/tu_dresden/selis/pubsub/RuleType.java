package de.tu_dresden.selis.pubsub;


import com.google.gson.annotations.SerializedName;

/**
 * Matching criteria, defines how to compare the value of the Message with the value defined in the Subscription.
 */
public enum RuleType {
    /**
     * Equals
     */
    @SerializedName("eq")
    EQ,

    /**
     * Not equals
     */
    @SerializedName("ne")
    NE,

    /**
     * Greater than
     */
    @SerializedName("gt")
    GT,

    /**
     * Greater or equals than
     */
    @SerializedName("ge")
    GE,

    /**
     * Lower than
     */
    @SerializedName("lt")
    LT,

    /**
     * Lower or equals than
     */
    @SerializedName("le")
    LE;

}
