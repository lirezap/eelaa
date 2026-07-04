## Fast, Reliable and Efficient Monetary General Ledger

Eelaa is a fast, reliable and efficient monetary general ledger that is able to handle 1 million transactions per
second.
It can be used as a GL module of a core banking platform or as wallet-as-a-service server.

Features included:

- Includes two types of ledgers, both are crash-safe and ACID compliant
    - WAL based: Very fast GL with compression enabled for handling more than a million of transactions per second
    - LMDB based: Fast and safe memory mapped GL able to handle 500k transactions per second (default)
- Multiple independent ledger instances in a single eelaa process
- Single and batch transactions processing
- Atomic (all or none) batch transactions processing
- Automatic recovery mechanism
- Source-destination accounting
- Real-time balances

Engineered around the best practices of designing high performance systems, like:

- Custom frame-based binary protocol
- Asynchronous and event driven TCP network stack using native transports with a batch-aware network protocol/triggers
- Custom atomic and crash-safe WAL implementation
- FFM and off-heap memory management to decrease GC pressure
- Fast compression mechanism
- Used native libraries where ever possible

### Test & Development Setup (macOS)

- JDK 26
    - ```curl -s "https://get.sdkman.io" | bash```
    - ```sdk install java 26.ea.13-graal```
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
export LIBRARIES_NATIVE_LZ4_PATH="/opt/homebrew/Cellar/lmdb/0.9.35/lib/liblmdb.dylib"
```

At final step run:

```bash
./scripts/test.sh
```

You are now ready to import the project into your IDE to test/develop further.
