# Cache Simulator

In order to run the simulation, you have to compile the cache simulator first.
```
javac src/CacheSimulator.java
```

Now we can use the cache simulator. The following command will run the cache simulator.
```
java src/CacheSimulator -L1s <L1s> -L1E <L1E> -L1b <L1b> -L2s <L2s> -L2E <L2E> -L2b <L2b> -t traces/<trace_file_name>
```

* -L1s <L1s>: Number of set index bits for L1 data/instruction cache (S = 2s is the number of sets)
* -L1E <L1E>: Associativity for L1 data/instruction cache (number of lines per set)
* -L1b <L1b>: Number of block bits for L1 data/instruction cache (B = 2b is the block size)
* -L2s <L2s>: Number of set index bits for L2 cache (S = 2s is the number of sets)
* -L2E <L2E>: Associativity for L2 cache (number of lines per set)
* -L2b <L2b>: Number of block bits for L2 cache (B = 2b is the block size)
* -t <tracefile>: Name of the trace file (see Reference Trace Files part below)

---

You can try the cache simulator with the following command:
```
java src/CacheSimulator -L1s 0 -L1E 2 -L1b 3 -L2s 1 -L2E 2 -L2b 3 -t traces/test_medium.trace
```