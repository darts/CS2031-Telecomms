#!/bin/bash
docker network create -d bridge --subnet 172.20.0.0/16 cs2031

docker create --name R1 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name R2 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name CONTROLLER --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name E1 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name E2 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash

docker network connect cs2031 R1
docker network connect cs2031 R2
docker network connect cs2031 E1
docker network connect cs2031 E2
docker network connect cs2031 CONTROLLER