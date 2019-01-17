package de.tu_dresden.selis.pubsub;

import com.google.gson.annotations.SerializedName;

/**
 * Subscription rule, contains of key, value and matching criteria defined in RuleType enum. Rule is used to define
 * matching on messages in the SELIS Publish/Subscribe system, therefore the Rule key should correspond to the Message
 * key.
 *
 * This is immutable implementation.
 */
public final class Rule {

    private String key;

    @SerializedName("val")
    private String value;

    @SerializedName("type")
    private ValueType valueType;

    @SerializedName("op")
    private RuleType ruleType;

    /**
     * Used only for JSON serialization/deserialization. Never use it from the Client code
     */
    @Deprecated
    public Rule() {
    }

    protected Rule(String key, String value, ValueType valueType, RuleType ruleType) {
        this.key = key;
        this.value = value;
        this.valueType = valueType;
        this.ruleType = ruleType;
    }

    /**
     * Create Rule where value is of type defined in ValueType. The exception can be thrown if value has invalid
     * type, therefore it is safer to use the static methods to create a Rule with known type.
     *
     * @param key - value which match the Message key
     * @param value - value which match the Message value according to ruleType
     * @param ruleType - type of matching criteria defined in RuleType enum
     * @throws PubSubArgumentException - throws if the value is not of type defined in ValueType
     * @return Rule object
     */
    public Rule(String key, Object value, RuleType ruleType) throws PubSubArgumentException {
        this.key = key;
        if (value != null) {
            this.value = value.toString();
            this.valueType = ValueType.byObjectType(value.getClass());
        }
        this.ruleType = ruleType;

        if (value != null && this.valueType == null) {
            throw new PubSubArgumentException("Invalid type of value: " + value.getClass() + ". Check allowed types in ValueType enum");
        }
    }

    /**
     * Create Rule where value is of int type
     *
     * @param key - value which match the Message key
     * @param value - value which match the Message value according to ruleType
     * @param ruleType - type of matching criteria defined in RuleType enum
     * @return Rule object
     */
    public static Rule intRule(String key, int value, RuleType ruleType) {
        return new Rule(key, String.valueOf(value), ValueType.INTEGER, ruleType);
    }

    /**
     * Create Rule where value is of double type
     *
     * @param key - value which match the Message key
     * @param value - value which match the Message value according to ruleType
     * @param ruleType - type of matching criteria defined in RuleType enum
     * @return Rule object
     */
    public static Rule doubleRule(String key, double value, RuleType ruleType) {
        return new Rule(key, String.valueOf(value), ValueType.DOUBLE, ruleType);
    }

    /**
     * Create Rule where value is of float type
     *
     * @param key - value which match the Message key
     * @param value - value which match the Message value according to ruleType
     * @param ruleType - type of matching criteria defined in RuleType enum
     * @return Rule object
     */
    public static Rule floatRule(String key, float value, RuleType ruleType) {
        return new Rule(key, String.valueOf(value), ValueType.FLOAT, ruleType);
    }

    /**
     * Create Rule where value is of String type
     *
     * @param key - value which match the Message key
     * @param value - value which match the Message value according to ruleType
     * @param ruleType - type of matching criteria defined in RuleType enum
     * @return Rule object
     */
    public static Rule stringRule(String key, String value, RuleType ruleType) {
        return new Rule(key, value, ValueType.STRING, ruleType);
    }

    /**
     * Create Rule where value is of boolean type
     *
     * @param key - value which match the Message key
     * @param value - value which match the Message value according to ruleType
     * @param ruleType - type of matching criteria defined in RuleType enum
     * @return Rule object
     */
    public static Rule booleanRule(String key, boolean value, RuleType ruleType) {
        String booleanValue = value ? "true" : "false";

        return new Rule(key, booleanValue, ValueType.BOOLEAN, ruleType);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public RuleType getRuleType() {
        return ruleType;
    }
}
