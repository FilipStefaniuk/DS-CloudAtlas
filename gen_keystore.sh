#!/bin/bash
keytool -genkey -alias querySigner -keyalg RSA -keysize 2048 -keystore keystore.jks -storepass $1 -keypass $2 -dname "CN=CloudAtlas" 2> /dev/null

