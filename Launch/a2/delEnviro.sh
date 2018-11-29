#!bin/bash
docker container stop CONTROLLER R1 R2 E1 E2
docker network rm cs2031
docker container rm CONTROLLER  
docker container rm R1
docker container rm R2
docker container rm E1
docker container rm E2
