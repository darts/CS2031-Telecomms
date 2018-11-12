#!/bin/bash
osascript -e 'tell application "Terminal" to do script "docker start -i publisher"'
osascript -e 'tell application "Terminal" to do script "docker start -i broker"'
osascript -e 'tell application "Terminal" to do script "docker start -i sub1"'
osascript -e 'tell application "Terminal" to do script "docker start -i sub2"'
