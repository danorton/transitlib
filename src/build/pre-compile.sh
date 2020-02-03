#!/usr/bin/env bash
set -e
# © 2020 Daniel Norton
echo "[INFO] ---------------------------- [ pre-compile ] ---------------------------"
#env
user=google
repo=transit
tag=33c329b0d07e72744de43319f839810a5df0b439
tarball=/tmp/$user-$repo-$tag.tgz
if [ ! -d src/main/java/com/$user/$repo ]; then
  echo "[INFO] Fetching google/transit source"
  curl --output $tarball -L https://github.com/$user/$repo/archive/$tag.tar.gz
  tar xzf $tarball
  protoc $repo-$tag/gtfs-realtime/proto/gtfs-realtime.proto --java_out=src/main/java
  rm -f $tarball
fi