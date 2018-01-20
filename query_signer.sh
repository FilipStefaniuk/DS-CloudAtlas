#!/bin/bash
java -Djava.rmi.server.codebase=file:./querySigner/target/querySigner-1.0-SNAPSHOT-jar-with-dependencies.jar -Djava.security.policy=signer.policy -jar ./querySigner/target/querySigner-1.0-SNAPSHOT-jar-with-dependencies.jar
