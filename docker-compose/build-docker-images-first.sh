#!/bin/bash
cd ../CounterAPI
./mvnw clean compile jib:dockerBuild -Djib.to.image=kafka-demo/counter-api:latest

cd ../GenMsgProducer
./mvnw clean compile jib:dockerBuild -Djib.to.image=kafka-demo/gen-msg-producer:latest

cd ../MsgConsumer
./mvnw clean compile jib:dockerBuild -Djib.to.image=kafka-demo/msg-consumer:latest

cd ../docker-compose
docker images|grep kafka-demo