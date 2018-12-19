#!bin/bash

docker container stop CONTROLLER R1 R2 E1 E2
docker network rm cs2031
docker container rm CONTROLLER  
docker container rm R1
docker container rm R2
docker container rm E1
docker container rm E2

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

cp ~/eclipse-workspace/TelecommsAssignment2/src/* //cs2031/a2/controller

osascript -e 'tell application "Terminal" to do script "docker start -i CONTROLLER"'
osascript -e 'tell application "Terminal" to do script "docker start -i R1"'
osascript -e 'tell application "Terminal" to do script "docker start -i R2"'
osascript -e 'tell application "Terminal" to do script "docker start -i E1"'
osascript -e 'tell application "Terminal" to do script "docker start -i E2"'
