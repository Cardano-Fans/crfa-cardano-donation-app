## Requirements
- JDK11

## Build
./gradlew shadowJar

## Run
java -Dmicronaut.config.files=application-mainnet.yml -jar crfa-token-app-1.0-all.jar

java -Dmicronaut.config.files=application-testnet.yml -jar crfa-token-app-1.0-all.jar
