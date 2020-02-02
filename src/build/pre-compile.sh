echo "BEGIN pre-compile ***********************"
#env
protoc submodules/google/transit/gtfs-realtime/proto/gtfs-realtime.proto --java_out=src/main/java
echo "  END pre-compile ***********************"
