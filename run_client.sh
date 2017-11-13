#!/bin/sh
#
# Distributed Systems Lab
# Copyright (C) Konrad Iwanicki, 2012-2014
#
# This file contains code samples for the distributed systems
# course. It is intended for internal use only.
#

#if [ $# -ne 1 ]; then
#  echo "Usage: run_client.sh <N>" >&2
#  exit 1
#fi

java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3
