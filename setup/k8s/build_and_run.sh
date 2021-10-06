#!/usr/bin/env bash
cd ../../services/hello-world-api
mvn  clean package
docker  build .   -t topas56/hello-world-api
#docker logout
#docker login -u -p
docker push topas56/hello-world-api
#Need to have k8s cluster
#kubectl create namespace hello-world-api
#k apply -f hello-world-api/