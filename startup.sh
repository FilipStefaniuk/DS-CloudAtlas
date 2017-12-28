#!/bin/bash
#REGISTRY_PORT=1324

#rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $REGISTRY_PORT &
#echo "rmiregistry started at port " $REGISTRY_PORT
java -Djava.rmi.server.codebase=file:/home/filip/Documents/cloudatlas/agent/target/agent-1.0-SNAPSHOT-jar-with-dependencies.jar -Djava.security.policy=server.policy -jar /home/filip/Documents/cloudatlas/agent/target/agent-1.0-SNAPSHOT-jar-with-dependencies.jar
#echo "agent bound and running"
#java -jar fetcher/target/fetcher-1.0-SNAPSHOT-jar-with-dependencies.jar &
#echo "fetcher running"
#./apache-tomcat-8.5.23/bin/startup.sh

# wait otherwise remote exception is thrown (change that)
#sleep 2
#java -jar fetcher/target/fetcher-1.0-SNAPSHOT-jar-with-dependencies.jar &
#echo "fetcher running"

#java -classpath /home/filip/Documents/cloudatlas/agent/target/agent-1.0-SNAPSHOT-jar-with-dependencies.jar -Djava.rmi.server.codebase=file:/home/filip/Documents/cloudatlas/agent/target/agent-1.0-SNAPSHOT.jar  pl.edu.mimuw.cloudatlas.agent.AgentServer &
