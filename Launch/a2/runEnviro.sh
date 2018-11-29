#!/bin/bash
osascript -e 'tell application "Terminal" to do script "docker start -i CONTROLLER"'
osascript -e 'tell application "Terminal" to do script "docker start -i R1"'
osascript -e 'tell application "Terminal" to do script "docker start -i R2"'
osascript -e 'tell application "Terminal" to do script "docker start -i E1"'
osascript -e 'tell application "Terminal" to do script "docker start -i E2"'