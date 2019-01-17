#!/bin/bash

CD=pwd

cd library && mvn clean install && cd ..;
cd example && mvn clean package && cd ..;

java -jar example/target/client-example-0.3.0-jar-with-dependencies.jar

