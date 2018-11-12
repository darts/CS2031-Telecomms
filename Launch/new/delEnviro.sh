#!bin/bash
docker container stop publisher broker sub1 sub2
docker network rm cs2031
docker container rm publisher
docker container rm broker
docker container rm sub1
docker container rm sub2
