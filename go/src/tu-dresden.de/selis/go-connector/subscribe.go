package pubsub

import (
	"bytes"
	"strconv"
)

type Subscription struct {
	AuthenticationHash string	`json:"authHash"`
	SubscriptionId string		`json:"subscriptionId"`
	Rules []Rule				`json:"data"`
}

type SubscriptionSocket struct {
	Host string					`json:"host"`
	Port int					`json:"port"`
}

func (s *SubscriptionSocket) address() string {
	var buffer bytes.Buffer
	buffer.WriteString(s.Host)
	buffer.WriteString(":")
	buffer.WriteString(strconv.Itoa(s.Port))
	return buffer.String()
}

func NewSubscription(authenticationHash string, subscriptionId string) *Subscription {
	return &Subscription{AuthenticationHash: authenticationHash, SubscriptionId: subscriptionId}
}

func (s *Subscription) addRule(rule *Rule) {
	s.Rules = append(s.Rules, *rule)
}

type Rule struct {
	Key string					`json:"key"`
	Value string				`json:"val"`
	Value_type string			`json:"type"`
	Rule_type string			`json:"op"`
}

type RuleType string
const (
	EQ RuleType = "eq"
	NE = "ne"
	GT = "gt"
	GE = "ge"
	LT = "lt"
	LE = "le"
)

func IntRule(key string, value int, ruleType RuleType) *Rule {
	return &Rule{Key: key, Value: strconv.Itoa(value), Value_type: "int", Rule_type: string(ruleType)}
}

func FloatRule(key string, value float64, ruleType RuleType) *Rule {
	float_value := strconv.FormatFloat(value, 'f', 6, 64)
	return &Rule{Key: key, Value: float_value, Value_type: "float", Rule_type: string(ruleType)}
}

func BooleanRule(key string, value bool, ruleType RuleType) *Rule {
	var bool_value = "false"
	if value {
		bool_value = "true"
	}
	return &Rule{Key: key, Value: bool_value, Value_type: "boolean", Rule_type: string(ruleType)}
}

func StringRule(key string, value string, ruleType RuleType) *Rule {
	return &Rule{Key: key, Value: value, Value_type: "string", Rule_type: string(ruleType)}
}

