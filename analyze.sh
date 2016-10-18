#!/bin/bash
mvn clean jacoco:prepare-agent install -DskipITs=true
mvn jacoco:prepare-agent-integration failsafe:integration-test
mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.1.1:sonar -Dsonar.host.url=http://localhost:9000
