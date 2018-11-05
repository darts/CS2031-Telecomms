#!/bin/bash
osascript -e 'tell application "Terminal" to do script "docker start -i publisher"'
osascript -e 'tell application "Terminal" to do script "docker start -i broker"'
osascript -e 'tell application "Terminal" to do script "docker start -i subscriber"'
