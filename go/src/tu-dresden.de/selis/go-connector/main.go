package pubsub

import "fmt"

import (
	"gopkg.in/resty.v1"
	"bytes"
	"strconv"
	"encoding/json"
	"io"
	"errors"
	"crypto/tls"
	"crypto/x509"
	"bufio"
	"io/ioutil"
)

type PubSub struct {
	certificate_file string
	host string
	port int
}

func NewPubSub(certificate_file string, host string, port int) *PubSub {
	return &PubSub{certificate_file: certificate_file, host: host, port: port}
}

func (p *PubSub) publish(msg map[string]string) error {
	jsonString, err := json.Marshal(msg)
	if err != nil {
		return errors.New(fmt.Sprintf("Could not convert message to the json: %s", err))
	}

	resp, err := resty.
	    SetRootCertificate(p.certificate_file).
	    R().
		SetHeader("Content-Type", "application/json").
		SetBody([]byte(jsonString)).
		Post(p.endpoint("/publish"))

	if resp != nil {
	}

	return err
}

func (p *PubSub) subscribe(subscription *Subscription, callback func(message string, err error)) error {
	jsonString, err := json.Marshal(subscription)
	if err != nil {
		return errors.New(fmt.Sprintf("Could not convert message to the json: %s", err))
	}
	response, err := resty.
		SetRootCertificate(p.certificate_file).
		R().
		SetHeader("Content-Type", "application/json").
		SetBody([]byte(jsonString)).
		SetResult(SubscriptionSocket{}).
		Post(p.endpoint("/subscribe"))
	if err != nil {
		return errors.New(fmt.Sprintf("Could not connect to PubSub REST API: %s", err))
	}

	subscriptionSocket := response.Result().(*SubscriptionSocket)

	roots := x509.NewCertPool()
	rootPEM, err := ioutil.ReadFile(p.certificate_file)
	if err != nil {
		return errors.New(fmt.Sprintf("Could not find certificate file %s", p.certificate_file))
	}
    ok := roots.AppendCertsFromPEM([]byte(rootPEM))
    if !ok {
		return errors.New(fmt.Sprintf("Failed to parse certificate %s", p.certificate_file))
    }
	conf := &tls.Config{RootCAs: roots}
	
	conn, err := tls.Dial("tcp", subscriptionSocket.address(), conf)
	if err != nil {
		return errors.New(fmt.Sprintf("Could not connect to the PubSub running on %s: %s", subscriptionSocket.address(), err))
	}

	authorisationToken := fmt.Sprintf("{\"authHash\": \"%s\", \"subscriptionId\": \"%s\"}%c", subscription.AuthenticationHash, subscription.SubscriptionId, '\n')
	_, err = conn.Write([]byte(authorisationToken))
	if err != nil {
		return errors.New(fmt.Sprintf("Could not send authorisation token %s to the PubSub running on %s: %s", authorisationToken, subscriptionSocket.address(), err))
	}
	go handleSubscription(conn, callback)

	return nil
}

func handleSubscription(conn io.Reader, callback func(message string, err error)) {
	reader := bufio.NewReader(conn)
	for {
		result, err := reader.ReadString('\n')
		if err != nil {
			callback("", errors.New(fmt.Sprintf("Could not receive message from the PubSub: %s", err)))
			return
		} else if result != "" {
			callback(string(result), nil)
		}
	}
}

func (p *PubSub) endpoint(path string) string {
	var buffer bytes.Buffer
	buffer.WriteString("https")
	buffer.WriteString("://")
	buffer.WriteString(p.host)
	buffer.WriteString(":")
	buffer.WriteString(strconv.Itoa(p.port))
	buffer.WriteString(path)
	return buffer.String()
}

