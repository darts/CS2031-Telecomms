#!/bin/bash
docker network create -d bridge --subnet 172.20.0.0/16 cs2031

docker create --name publisher --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name broker --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name sub1 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name sub2 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash

docker network connect cs2031 publisher
docker network connect cs2031 broker
docker network connect cs2031 sub1
docker network connect cs2031 sub2