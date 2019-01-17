package pubsub

import (
	"testing"
	"fmt"
	"time"
)

func TestPublishSubscribe(t *testing.T) {
	fmt.Println("Starting subscribe test")
	pubsub := NewPubSub("../../../../certificates/root.crt", "localhost", 20000)

	subscription := NewSubscription("clientIdHash2", "subId3")
	subscription.addRule(StringRule("_type", "PKI", EQ))
	callback := func(message string, err error) {
			if err != nil {
				t.Errorf("Could not subscribe: %s", err)
			} else {
				fmt.Println("Got new message: ", message)
			}
		}
	fmt.Println("Subscribing to: ", subscription)
	err := pubsub.subscribe(subscription, callback)
	if err != nil {
		t.Errorf("Could not subscribe: %s", err)
	}

	for {
		go Publish(pubsub, t)
		time.Sleep(time.Second)
	}

	time.Sleep(time.Second * 30)
}

func Publish(pubsub *PubSub, t *testing.T)  {
	msg := map[string]string {
		"_type": "PKI",
		"_subtype": "avg_price",
		"supplier_id": "ABC2138",
		"value": "1908",
	}
	fmt.Println("Publishing new message: ", msg)
	err := pubsub.publish(msg)
	if err != nil {
		t.Errorf("Could not publish message: %s", err)
	}
}