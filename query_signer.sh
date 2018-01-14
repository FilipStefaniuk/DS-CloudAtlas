#!/bin/bash
java -Djava.rmi.server.codebase=file:/home/filip/Documents/cloudatlas/querySigner/target/querySigner-1.0-SNAPSHOT-jar-with-dependencies.jar -Djava.security.policy=signer.policy -jar /home/filip/Documents/cloudatlas/querySigner/target/querySigner-1.0-SNAPSHOT-jar-with-dependencies.jar
