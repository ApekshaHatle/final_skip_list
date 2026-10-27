// VERSION 2: COUNTER
public class CounterSkipList extends SkipList {
    static final int MIN_COUNT = 4;      
    double total = 0;                    

    CounterSkipList(long seed) {
        super(seed);
    }

    int targetHeight(Node u) {
        if (u.count < MIN_COUNT) return u.baseHeight;
        double fairShare = total / size;
        double x = u.count / fairShare;              
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
