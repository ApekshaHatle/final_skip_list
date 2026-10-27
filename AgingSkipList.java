import java.util.ArrayList;

// VERSION 3: COUNTER + OUR AGING (the novelty).
//
// Normal aging: every now and then stop everything and halve ALL counters.
// Our aging has two simple parts and never stops the list:
//
//  1) FADING WEIGHT: instead of adding 1 for every search, we add a weight that
//     gets slightly bigger with every search. So a new search counts a bit more
//     than an old one. Old searches fade away on their own and we never have to
//     go through all the counters.
//
//  2) SWEEPER: after every search we look at a couple of tall keys (round robin).
//     If the key has cooled down, its tower is cut down. Cooling down is
//     therefore noticed little by little, with no big pause.
public class AgingSkipList extends SkipList {
    static final int MIN_COUNT = 4;
    static final int SWEEP = 2;          // how many tall keys the sweeper checks per search
    static final int SLACK = 1;          // only cut a tower if it is 2+ levels too tall

    double halfLife;                     // after this many searches a hit counts half as much
    double growth;                       // weight multiplier per search
    double weight = 1.0;                 // weight of the newest search
    double total = 0;                    // sum of all weights

    ArrayList<Node> tall = new ArrayList<>();   // keys that are taller than their base height
    int sweepPos = 0;

    AgingSkipList(long seed, double halfLife) {
        super(seed);
        this.halfLife = halfLife;
        this.growth = Math.pow(2.0, 1.0 / halfLife);
    }

    // Same rule as the counter version, but the counts are measured in
    // "recent searches" (divided by the current weight).
    int targetHeight(Node u) {
        double recentHits = u.count / weight;
        if (recentHits < MIN_COUNT) return u.baseHeight;
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

        weight *= growth;                // part 1: newer searches weigh more
        u.count += weight;
        total += weight;

        int before = u.height;
        promote(u, targetHeight(u));
        if (u.height > before && !u.inTall) {
            u.inTall = true;
            tall.add(u);
        }

        sweep();                         // part 2: check a few tall keys
        if (weight > 1e100) rescale();   // keep the numbers small (very rare)
        return hops;
    }

    void sweep() {
        for (int s = 0; s < SWEEP && !tall.isEmpty(); s++) {
            if (sweepPos >= tall.size()) sweepPos = 0;
            Node v = tall.get(sweepPos);
            int t = targetHeight(v);
            if (v.height > t + SLACK) demote(v, t);
            if (v.height == v.baseHeight) {          // back to normal: remove from list
                v.inTall = false;
                Node last = tall.remove(tall.size() - 1);
                if (last != v) tall.set(sweepPos, last);
            } else {
                sweepPos++;
            }
        }
    }

    void rescale() {
        for (Node x = head.next[0]; x != null; x = x.next[0]) x.count /= weight;
        total /= weight;
        weight = 1.0;
    }
}
