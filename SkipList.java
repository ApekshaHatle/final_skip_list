import java.util.Random;

// VERSION 1: BASELINE
public class SkipList {
    static final int MAX_LEVEL = 20;
    static final int EXTRA = 2;     // extra levels given to popular keys (chosen on seeds 1-2)

    Node head = new Node(Integer.MIN_VALUE, MAX_LEVEL, MAX_LEVEL);
    int level = 1;          // highest level currently in use
    int size = 0;
    Random rand;
    Node[] update = new Node[MAX_LEVEL];   // remembers where the last search turned down
    int lastHops = 0;
    long promotions = 0, demotions = 0;

    SkipList(long seed) {
        rand = new Random(seed);
    }

    int randomHeight() {
        int h = 1;
        while (h < MAX_LEVEL && rand.nextBoolean()) h++;
        return h;
    }

    void insert(int key) {
        Node[] up = new Node[MAX_LEVEL];
        Node x = head;
        for (int i = MAX_LEVEL - 1; i >= 0; i--) {
            while (x.next[i] != null && x.next[i].key < key) x = x.next[i];
            up[i] = x;
        }
        if (x.next[0] != null && x.next[0].key == key) return;   // already present
        int h = randomHeight();
        Node n = new Node(key, h, MAX_LEVEL);
        for (int i = 0; i < h; i++) {
            n.next[i] = up[i].next[i];
            up[i].next[i] = n;
        }
        if (h > level) level = h;
        size++;
    }

    Node search(int key) {
        int hops = 0;
        Node x = head;
        for (int i = level - 1; i >= 0; i--) {
            while (x.next[i] != null && x.next[i].key < key) {
                x = x.next[i];
                hops++;
            }
            update[i] = x;
            hops++;
            Node nx = x.next[i];
            if (nx != null && nx.key == key) {
                lastHops = hops;
                return nx;
            }
        }
        lastHops = hops;
        return null;
    }

    int access(int key) {
        search(key);
        return lastHops;
    }

    int heightFor(Node u, double x) {
        int boost = (int) (Math.log(x) / Math.log(2)) + EXTRA;
        int t = u.baseHeight + boost;
        if (t > MAX_LEVEL) t = MAX_LEVEL;
        return t;
    }

    void promote(Node u, int target) {
        while (u.height < target) {
            int i = u.height;
            if (i >= level) {
                update[i] = head;
                level = i + 1;
            }
            u.next[i] = update[i].next[i];
            update[i].next[i] = u;
            u.height++;
            promotions++;
        }
    }

    void demote(Node v, int target) {
        Node[] up = new Node[MAX_LEVEL];
        Node x = head;
        for (int i = level - 1; i >= 0; i--) {
            while (x.next[i] != null && x.next[i].key < v.key) x = x.next[i];
            up[i] = x;
        }
        for (int i = v.height - 1; i >= target; i--) {
            up[i].next[i] = v.next[i];
            v.next[i] = null;
            demotions++;
        }
        v.height = target;
        while (level > 1 && head.next[level - 1] == null) level--;
    }

    boolean check() {
        for (int i = 0; i < MAX_LEVEL; i++) {
            Node x = head.next[i];
            int prev = Integer.MIN_VALUE;
            while (x != null) {
                if (x.key <= prev) return false;
                if (x.height <= i) return false;
                prev = x.key;
                x = x.next[i];
            }
        }
        return true;
    }
}
