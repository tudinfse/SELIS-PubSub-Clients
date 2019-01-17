package de.tu_dresden.selis.pubsub;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines criteria required to register new subscription in the SELIS Publish/Subscribe system.
 *
 * This mutable class allowing to add and remove subscription' Rules.
 */
public final class Subscription {

    public static final int MAX_NUMBER_OF_RULES = 100;

    @SerializedName("authHash")
    private String authenticationHash;

    private String subscriptionId = "";

    /**
     * Collection of Rules used to match messages with the Subscription. All of the Rules in this collection
     * creates the conjunction of matching criteria.
     */
    @SerializedName("data")
    private List<Rule> rules = new LinkedList<>();

    // for JSON serialization/deserialization
    @Deprecated
    public Subscription() {
    }

    /**
     * Create the Subscription with empty list of Rules
     *
     * @param authenticationHash - authentication hash used by the SELIS Publish/Subscribe system to authenticate the Subscriber
     */
    public Subscription(String authenticationHash) {
        this.authenticationHash = authenticationHash;
    }
    /**
     * Create the Subscription with empty list of Rules
     *
     * @param authenticationHash - authentication hash used by the SELIS Publish/Subscribe system to authenticate the Subscriber
     * @param subscriptionId - subscription id used by the SELIS Publish/Subscribe system to authenticate the Subscriber
     */
    @Deprecated
    public Subscription(String authenticationHash, String subscriptionId) {
        this.authenticationHash = authenticationHash;
        this.subscriptionId = subscriptionId;
    }

    /**
     * Create the Subscription giving the List of the Rules.
     *
     * @param authenticationHash - authentication hash used by the SELIS Publish/Subscribe system to authenticate the Subscriber
     * @param rules - List of Rules
     * @throws PubSubArgumentException - thrown of the maximum number of Rules defined in MAX_NUMBER_OF_RULES is exceeded.
     */
    public Subscription(String authenticationHash, List<Rule> rules) throws PubSubArgumentException {
        if(rules.size() > MAX_NUMBER_OF_RULES) {
            throw new PubSubArgumentException("Subscription cannot have more than " + MAX_NUMBER_OF_RULES + " rules");
        }

        this.authenticationHash = authenticationHash;
        this.rules = rules != null ? rules : new LinkedList<Rule>();
    }

    /**
     * Create the Subscription giving the List of the Rules.
     *
     * @param authenticationHash - authentication hash used by the SELIS Publish/Subscribe system to authenticate the Subscriber
     * @param subscriptionId - subscription id used by the SELIS Publish/Subscribe system to authenticate the Subscriber
     * @param rules - List of Rules
     * @throws PubSubArgumentException - thrown of the maximum number of Rules defined in MAX_NUMBER_OF_RULES is exceeded.
     */
    @Deprecated
    public Subscription(String authenticationHash, String subscriptionId, List<Rule> rules) throws PubSubArgumentException {
        if(rules.size() > MAX_NUMBER_OF_RULES) {
            throw new PubSubArgumentException("Subscription cannot have more than " + MAX_NUMBER_OF_RULES + " rules");
        }

        this.authenticationHash = authenticationHash;
        this.subscriptionId = subscriptionId;
        this.rules = rules != null ? rules : new LinkedList<Rule>();
    }

    /**
     * Adds a Rule to the Subscription.
     *
     * @throws PubSubArgumentException - thrown of the maximum number of Rules defined in MAX_NUMBER_OF_RULES is exceeded.
     * @param rule - rule to add
     */
    public void add(Rule rule) throws PubSubArgumentException {
        if (rules.size() > MAX_NUMBER_OF_RULES) {
            throw new PubSubArgumentException("Subscription cannot have more than " + MAX_NUMBER_OF_RULES + " rules");
        }
        this.rules.add(rule);
    }

    /**
     * Removes the Rule from the rules assigned to this Subscription
     *
     * @param rule - rule to remove
     */
    public void remove(Rule rule) {
        this.rules.remove(rule);
    }

    public String getAuthenticationHash() {
        return authenticationHash;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns the copy of the Rules defined in this Subscription
     * @return
     */
    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }
}