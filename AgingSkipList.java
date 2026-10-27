import java.util.ArrayList;

// VERSION 3: COUNTER + shifted AGING 
public class AgingSkipList extends SkipList {
    static final int MIN_COUNT = 4;
    static final int SWEEP = 2;          
    static final int SLACK = 1;          

    double halfLife;                     
    double growth;                       
    double weight = 1.0;                 
    double total = 0;                    

    ArrayList<Node> tall = new ArrayList<>();   
    int sweepPos = 0;

    AgingSkipList(long seed, double halfLife) {
        super(seed);
        this.halfLife = halfLife;
        this.growth = Math.pow(2.0, 1.0 / halfLife);
    }

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

        weight *= growth;                
        u.count += weight;
        total += weight;

        int before = u.height;
        promote(u, targetHeight(u));
        if (u.height > before && !u.inTall) {
            u.inTall = true;
            tall.add(u);
        }

        sweep();                         
        if (weight > 1e100) rescale();   
        return hops;
    }

    void sweep() {
        for (int s = 0; s < SWEEP && !tall.isEmpty(); s++) {
            if (sweepPos >= tall.size()) sweepPos = 0;
            Node v = tall.get(sweepPos);
            int t = targetHeight(v);
            if (v.height > t + SLACK) demote(v, t);
            if (v.height == v.baseHeight) {          
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
