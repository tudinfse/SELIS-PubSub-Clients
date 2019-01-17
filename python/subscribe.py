#!/usr/bin/env python

import sys
import socket, ssl
import time
import json
import urllib2
import StringIO

pubsubServiceURL = "https://selis-gw.cslab.ece.ntua.gr:20000/"
authHash = "THIS_IS_SECURITY_TOKEN_RETRIEVED_FROM_SELIS_SSO"


def createSocket(host, port):
    context = ssl.create_default_context()
    context.load_verify_locations("dev_selis_ca.crt")
    s = context.wrap_socket(socket.socket(socket.AF_INET), server_hostname=host)
    s.connect((host, int(port)))
    return s

if __name__ == '__main__':
    ctx = ssl.create_default_context(cafile="dev_selis_ca.crt")
    
    message = {
        "authHash" : authHash,
        "data" : [{ "key" : "Warehouse", "val" : "Test", "type" : "string", "op" : "eq" }]}
    request = urllib2.Request(pubsubServiceURL+"subscribe", json.dumps(message), { 'Content-Type' : 'application/json' })
    response = urllib2.urlopen(request, context=ctx)
    answer = json.loads(response.read())

    clientSocket = createSocket(answer["host"], answer["port"])
    clientSocket.send(json.dumps({ "authHash" : authHash, "subscriptionId" : answer["subscriptionId"] })+"\n")

    while True:
        buff = StringIO.StringIO(2048)
        while True:
            data = clientSocket.recv(1)
            buff.write(data)
            if '\n' in data: 
                print json.loads(buff.getvalue().splitlines()[0])
                buff = StringIO.StringIO(2048)
    clientSocket.close()
