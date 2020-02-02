#!/usr/bin/env bash
# © 2020 Daniel Norton
echo "[INFO] ---------------------------- [ pre-compile ] ---------------------------"
#env
protoc submodules/google/transit/gtfs-realtime/proto/gtfs-realtime.proto --java_out=src/main/java
