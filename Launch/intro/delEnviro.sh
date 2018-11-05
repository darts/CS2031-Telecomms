#!bin/bash
docker container stop client server
docker network rm cs2031
docker container rm client
docker container rm server
