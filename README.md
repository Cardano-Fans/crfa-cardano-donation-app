
## Introduction
This is a donation application that automatically donates to certain entities / addresses specified in configuration file. It can donate either on epoch or super epoch (6 epochs) cadence.

## Problem description
One of common problems in Cardano is that apart from Catalyst and running a pool there is no way to fund certain development projects. Some people setup a stake pool but even this doesn't guarantee that delegators will select such a pool. In addition certain number of developers is not interested in managing a stake pool.

We need alternatives. This application allows anyone, delegator, user, spo to setup regular delegations to projects they support.

## Requirements
- JDK11

## Build
./gradlew shadowJar

## Run
java -Dmicronaut.config.files=application-mainnet.yml -jar crfa-cardano-donation-app-1.0-all.jar

java -Dmicronaut.config.files=application-testnet.yml -jar crfa-cardano-donation-app-1.0-all.jar
