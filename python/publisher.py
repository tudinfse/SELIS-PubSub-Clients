#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import socket, ssl
import time
import json
import urllib2
import random

pubsubServiceURL = "https://selis-gw.cslab.ece.ntua.gr:20000/"

if __name__ == '__main__':
    ctx = ssl.create_default_context(cafile="dev_selis_ca.crt")

    while True:
        message = {
            "OrdAgencyCode": "",
            "OrdDateTime": "2016-11-29T19:04:00",
            "OrdDeliveryDateTime": "2017-03-27T07:41:34",
            "OrdVoucherDateTime": "2016-11-30T00:00:00",
            "OrdVoucherId": 693472,
            "OrdVoucherNumber": "69823",
			"Warehouse": "Test"
        }   

        request = urllib2.Request(pubsubServiceURL+"publish", json.dumps(message), { 'Content-Type' : 'application/json' })
        json.load(urllib2.urlopen(request, context=ctx))

        time.sleep(2)

