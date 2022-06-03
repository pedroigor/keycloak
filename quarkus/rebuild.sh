#!/usr/bin/env sh
set -euxo pipefail

(cd .. && mvn clean install -f config-api/pom.xml --offline)

mvn clean package -DskipTests --offline

tar -xvf dist/target/keycloak-999-SNAPSHOT.tar.gz -C dist/target/
