#!/usr/bin/env bash

mvn clean package

echo 'Copy files...'

scp -i scripts/12345678.pem \
  target/diplom-0.1.jar \
  ubuntu@ec2-18-218-155-111.us-east-2.compute.amazonaws.com:/home/ubuntu/

echo 'Restart server...'

ssh -i scripts/12345678.pem ubuntu@ec2-18-218-155-111.us-east-2.compute.amazonaws.com << EOF

pgrep java | xargs kill -9
nohup java -jar diplom-0.1.jar > log.txt &

EOF

echo 'Bye'