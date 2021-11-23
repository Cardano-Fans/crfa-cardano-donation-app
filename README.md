## Introduction
This is a donation application that automatically donates to certain entities / addresses specified in configuration file. It can donate either on epoch or super epoch (6 epochs) basis, which we call internally cadence.

## Problem description
One of common problems in Cardano is that apart from Catalyst and running a pool there is no way to fund certain development projects or charities. Some people setup a stake pool but even this doesn't guarantee that delegators will select such a pool, they may prefer other pools. In addition certain number of people are not interested in managing a stake pool. This all doesn't mean that one doesn't want to contribute, this could be as little as 1 ADA per month. If everybody contributes 1 ADA it adds up.

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

## Configuration file
```
  micronaut:
  application:
    # don't touch it :)
    name: crfa-cardano-donation-app
  server:
    # port on which app runs
    port: 9095

donation:
  # how often transaction should be done, can be: EPOCH or SUPER_EPOCH
  cadence: EPOCH
  # to whom donations should be done
  entities:
    # donation wallet and how much ADA should be donated
    test-wallet1: 1
  # who donated - this will be then set in transaction metadata, this can be anything, nickname / email / name
  donor: anonymous

# complete list of entities - of course one can add own entities(!)
entities:
  test-wallet1:
    # entity name
    name: test-wallet1
    address: addr_test1qrer5vurzl4krj7tlwvqy0kgtad8r53uug5276868xshcfkz2uggmz3kw8kt7yvkzue02nhfk6mhqjmta85qsd5etjzsmf3sfw

# dry run mode means it will actually not send any value, just for testing
dryRunMode: false
# what kind of environment it is, can be: testnet or mainnet
env: testnet
# blockfrost project Id
projectId: <FILL_YOUR_BLOCKFROST_PROJECT_ID>
# wallet file name in home folder  
walletFilename: .crfa-donation-app-wallet-testnet.dat
# sqlite3 database location
dbPath: crfa-cardano-donation-app-testnet.db
```

## Recommendations
- we recommend to stake your cold wallet
- we recommend not to host this app in the cloud environment but rather on private home or office server
- we recommend to have small part of your ADA in this wallet, after all this is a cold wallet, not hardware wallet
- we recommend that other users in the system have no access to home folder to read pass-phrase or even better only one person or trusted people having access to home or office server
- due to security vulnerabilities we would discourage users from running this app on Windows and rather avoid running on osx, linux or *nix based systems are really the best for this app
  
## Build
```
./gradlew shadowJar
```

## Run without systemd (not recommended, only for testing)
### main-net
```
java -Dmicronaut.config.files=application-mainnet.yml -jar crfa-cardano-donation-app-<versio>.jar
```
  
### test-net
```
java -Dmicronaut.config.files=application-testnet.yml -jar crfa-cardano-donation-app-<version>.jar
```  
