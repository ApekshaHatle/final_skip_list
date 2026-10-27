// VERSION 2: COUNTER. Every search adds 1 to the key's counter.
// Popular keys get taller towers. Nothing is ever forgotten (no aging).
public class CounterSkipList extends SkipList {
    static final int MIN_COUNT = 4;      // a key needs at least 4 hits before it can grow
    double total = 0;                    // total number of searches counted

    CounterSkipList(long seed) {
        super(seed);
    }

    // Height this key "deserves": a key that is 2^k times more popular than an
    // average key gets about k extra levels.
    int targetHeight(Node u) {
        if (u.count < MIN_COUNT) return u.baseHeight;
        double fairShare = total / size;
        double x = u.count / fairShare;              // how many times more popular than average
        if (x < 1) return u.baseHeight;
        return heightFor(u, x);
    }

    @Override
    int access(int key) {
        Node u = search(key);
        int hops = lastHops;
        if (u == null) return hops;
        u.count += 1;
        total += 1;
        promote(u, targetHeight(u));
        return hops;
    }
}
