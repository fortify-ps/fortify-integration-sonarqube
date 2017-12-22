#! /bin/bash
set -x
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PLUGIN_MVN_DIR=$SCRIPT_DIR/..
DOCKER_DIR=$SCRIPT_DIR
PLUGIN_VERSION=1.0-SNAPSHOT

# Stop any running SonarQube instance
docker rm -f sonarqube 2>/dev/null

# Stop the script if any errors occur after this point
set -e

# Build the Maven project
cd $PLUGIN_MVN_DIR
mvn clean package

# Copy plugin to Docker directory and build new image
cd $DOCKER_DIR
cp -f $PLUGIN_MVN_DIR/target/fortify-sonarqube-plugin-$PLUGIN_VERSION.jar ./fortify-sonarqube-plugin.jar
docker build -t rsenden/sonarqube:fortify .

# Run Fortify SonarQube image
docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 rsenden/sonarqube:fortify

# Show logs, and remove docker container when logs command is interrupted
set +e
docker logs -f sonarqube
docker rm -f sonarqube
