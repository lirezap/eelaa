## Fast, Reliable and Efficient Monetary General Ledger

Eelaa is a fast, reliable and efficient monetary general ledger that is able to handle one million transactions per
second. It can be used as a GL module of a core banking platform or as wallet infrastructure.

**Features included:**

- Includes two types of ledgers, both are crash-safe and ACID compliant
    - Fast: WAL-based, NTP-dependent and very fast GL able to handle a million of transactions per second.
    - Default: Fast and safe memory mapped GL able to handle 500K transactions per second.
- Multiple independent ledger instances in a single eelaa process
- Single and batch transactions processing
- Atomic (all or none) batch transactions processing
- Automatic recovery mechanism
- Source-destination accounting
- Real-time balances
- Idempotent services
- Replay attack protection
- TLS v1.3

**Engineered around the best practices of designing high performance systems, like:**

- Custom frame-based binary protocol
- Asynchronous and event driven TCP network stack using native transports with a batch-aware network protocol/triggers
- Custom, efficient and very fast micro HTTP server based on pure netty
- Custom atomic and crash-safe WAL implementation
- FFM and off-heap memory management to decrease GC pressure
- Fast compression mechanism
- Having mechanical sympathy for the hardware it’s running on (lock-free implementations)
- Used native libraries where ever possible

---

### Clustering & Replication

Eelaa focuses on being a fast, single-node ledger engine. High availability and storage replication are intentionally
delegated to mature infrastructure such as DRBD instead of implementing distributed consensus within the server.

---

### REST APIs

Documentation can be found at [REST APIs Documentation](REST_APIs_DOC.md).

---

### Binary Protocol Specification

Documentation in progress.

---

### Test & Development Setup (macOS)

- JDK 26
    - ```curl -s "https://get.sdkman.io" | bash```
    - ```sdk install java 26.0.2-oracle```
- LZ4 Compression Library
    - ```/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"```
    - ```brew install lz4```
- LMDB
    - ```brew install lmdb```

Then after, set the following environment variable:

```bash
export LIBRARIES_NATIVE_LZ4_PATH="/the/path/to/liblz4.dylib"
```

```bash
export LIBRARIES_NATIVE_LMDB_PATH="/the/path/to/liblmdb.dylib"
```

For example:

```bash
export LIBRARIES_NATIVE_LZ4_PATH="/opt/homebrew/Cellar/lz4/1.10.0/lib/liblz4.dylib"
```

```bash
export LIBRARIES_NATIVE_LMDB_PATH="/opt/homebrew/Cellar/lmdb/0.9.35/lib/liblmdb.dylib"
```

At final step run:

```bash
./scripts/test.sh
```

You are now ready to import the project into your IDE to test/develop further.

---

### Dockerize Project

To dockerize the project you need docker engine or docker desktop to be installed; See https://www.docker.com to follow
the installation instructions.

Then after, run:

```./scripts/dockerize.sh```

The Dockerfile also includes installation steps for both required native libraries.
