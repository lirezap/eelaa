#!/usr/bin/env sh

echo "[INFO] NATIVE_LIBRARIES_LZ4_PATH environment variable must be defined for all tests to be passed"
echo "[INFO] NATIVE_LIBRARIES_LMDB_PATH environment variable must be defined for all tests to be passed"

./mvnw clean test -DargLine="-Xmx8g" clean
