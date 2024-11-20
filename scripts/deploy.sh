#!/usr/bin/env bash

#mvn clean package

echo 'Copy files...'

scp target/diplom-0.1.jar root@45.90.46.253:/home/

echo 'Restart server...'

ssh root@45.90.46.253 << EOF

pgrep java | xargs kill -9
nohup java -jar diplom-0.1.jar > log.txt &

EOF

echo 'Bye'