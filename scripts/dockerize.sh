#!/usr/bin/env sh

./mvnw clean package -DskipTests

# Extracting the version number from the jar filename ...
VERSION=$(ls target | grep -E '.*?\.jar$' | sed -E 's/^.*-([0-9.]+).*\.jar$/\1/')
IMAGE_NAME=eelaa
IMAGE_TAG=$VERSION

docker build -t $IMAGE_NAME:$IMAGE_TAG .
