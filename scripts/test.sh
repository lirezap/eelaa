#!/usr/bin/env sh

echo "[INFO] LIBRARIES_NATIVE_LZ4_PATH environment variable must be defined for all tests to be passed"
echo "[INFO] LIBRARIES_NATIVE_LMDB_PATH environment variable must be defined for all tests to be passed"

./mvnw clean test -Dtest='LMDBManagerTest'
./mvnw clean test -DargLine="-Xmx8g" -Dtest='*,!LMDBManagerTest' clean
