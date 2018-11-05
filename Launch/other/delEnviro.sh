#!bin/bash
docker container stop publisher broker subscriber
docker network rm cs2031
docker container rm publisher
docker container rm broker
docker container rm subscriber
