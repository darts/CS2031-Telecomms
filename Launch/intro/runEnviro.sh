#!/bin/bash
osascript -e 'tell application "Terminal" to do script "docker start -i server"'
osascript -e 'tell application "Terminal" to do script "docker start -i client"'
