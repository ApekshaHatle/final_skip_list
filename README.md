# Skip list with popularity counters and "fading" aging

Plain Java, no libraries.

## The three versions

| Version | File | What it does |
|---|---|---|
| 1. Baseline | `SkipList.java` | Ordinary skip list. Random heights, never change. |
| 2. Counter | `CounterSkipList.java` | Counts searches per key. Popular keys get taller towers. Nothing is forgotten. |
| 3. Counter + our aging | `AgingSkipList.java` | Same as 2, but old searches fade away by themselves and a sweeper cuts down towers of keys that cooled off. |

## Our aging (the new idea)

Normal aging halves every counter on a timer, which stops the whole list for a moment.
Ours has two small parts and never stops:

1. Fading weight. Every search adds a weight to the counter, and the weight grows a little with each search,
   so recent searches count more than old ones. Nothing has to be divided or reset.
2. Sweeper. After each search, 2 tall keys are checked (round robin). If a key has cooled down, its tower is cut.

## Other files

* `Node.java` one element of the list
* `Workloads.java` makes the search sequences (uniform, 90-10, 95-5, 99-1, zipf, shifting)
* `Demo.java` prints the real skip list (towers, chains on each level, table of elements)
* `Experiment.java` compares the 3 versions and prints the results table

## Run

    javac -d out *.java
    java -cp out Demo                       # see the actual structure
    java -cp out Experiment 21 10 100000    # results (seeds 21-30, half-life 100000)

## Settings (chosen on seeds 1-2, then frozen; results reported on seeds 21-30)

* `MIN_COUNT = 4` a key needs 4 hits before it can grow
* `EXTRA = 2` extra levels for popular keys
* half-life = 100,000 searches
* `SWEEP = 2`, `SLACK = 1`

## Limits

* Only insert and search are implemented (no delete).
* One thread only.
* Tests use made-up search patterns, not real traces.
* Rule of thumb: a skip list is already fast, so gains come only when some keys are searched much more than others.
