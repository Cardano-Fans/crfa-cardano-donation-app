## Introduction
This is a donation application that automatically donates to certain entities / addresses specified in configuration file. It can donate either on epoch or super epoch (6 epochs) basis, which we call internally cadence.

## Problem description
One of common problems in Cardano is that apart from Catalyst and running a pool there is no way to fund certain development projects or charities. Some people setup a stake pool but even this doesn't guarantee that delegators will select such a pool, they may prefer other pools. In addition certain number of people are not interested in managing a stake pool. This all doesn't mean that one doesn't want to contribute, this could be as little as 1 ADA per month. If everybody contributes 1 ADA it ads up.

We need alternatives. This application allows anyone, delegator, user, spo to setup regular delegations to projects or charities they support.

### 

## Requirements
- at least JDK11 (feature request: native image / docker image)
- Intel / AMD
- Linux or OSX
- Currently ARM: Raspberry PI 3 and 4 are not supported (feature request: Raspberry PI support)

## Installation
- Create test and / or mainnet project on https://blockfrost.io, you need projectId inside of configuration file
- put 15 or 24 words file into home directory. Ideally this should be new cold wallet (hardware wallets are not supported for obvious reasons). Name of the file is controlled by a configuration variable: walletFilename
- make sure to set this file readable only by the user running application, e.g. chmod 600 
- Download latest jar from https://github.com/Cardano-Fans/crfa-cardano-donation-app/releases and install application into e.g. /opt/crfa-donation-app/crfa-cardano-donation-app-<version>.jar
- for linux copy systemd file from contrib folder: crfa-donation-app.service into /etc/systemd/system and run sudo systemctl enable crfa-donation-app and copy crfa-donation-app.sh from contrib folder into /opt/crfa-donation-app
- run via sudo systemctl start crfa-donation-app and observe log file via sudo systemctl -u crfa-donation-app -f
- 

## Configuration file
```
```
  
## Build
./gradlew shadowJar

## Run without systemd (not recommended, only for testing)
### main-net
java -Dmicronaut.config.files=application-mainnet.yml -jar crfa-cardano-donation-app-<versio>.jar

### test-net
java -Dmicronaut.config.files=application-testnet.yml -jar crfa-cardano-donation-app-<version>.jar
