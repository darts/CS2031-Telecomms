#!/bin/bash
docker network create -d bridge --subnet 172.20.0.0/16 cs2031

docker create --name client --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name server --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash

docker network connect cs2031 client
docker network connect cs2031 server
