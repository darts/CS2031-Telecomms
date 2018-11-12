#!bin/bash

docker container stop publisher broker sub1 sub2
docker network rm cs2031
docker container rm publisher
docker container rm broker
docker container rm sub1
docket container rm sub2

docker network create -d bridge --subnet 172.20.0.0/16 cs2031

docker create --name publisher --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name broker --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docker create --name sub1 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash
docket create --name sub2 --cap-add=ALL -ti -v /cs2031:/cs2031 java /bin/bash

docker network connect cs2031 publisher
docker network connect cs2031 broker
docker network connect cs2031 sub1
docker network connect cs2031 sub2

osascript -e 'tell application "Terminal" to do script "docker start -i publisher"'
osascript -e 'tell application "Terminal" to do script "docker start -i broker"'
osascript -e 'tell application "Terminal" to do script "docker start -i sub1"'
osascript -e 'tell application "Terminal" to do script "docker start -i sub2"'
