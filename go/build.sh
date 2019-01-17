#!/bin/bash
set -x

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

GOPATH=$DIR go clean
GOPATH=$DIR go get gopkg.in/resty.v1
GOPATH=$DIR go install tu-dresden.de/selis/go-connector

# copy root.crt to the location where test file is
GOPATH=$DIR go test tu-dresden.de/selis/go-connector -v

