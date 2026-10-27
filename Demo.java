import java.util.Random;

// Shows the real skip list (every level, every element) for the 3 versions on a tiny list.
// Usage: java Demo
public class Demo {
    static final int N = 16;            // keys 1..16

    public static void main(String[] args) {
        SkipList[] lists = {new SkipList(7), new CounterSkipList(7), new AgingSkipList(7, 150)};
        String[] names = {"1. BASELINE (plain skip list)", "2. COUNTER", "3. COUNTER + OUR AGING"};
        for (SkipList l : lists) for (int k = 1; k <= N; k++) l.insert(k);

        System.out.println("A skip list with keys 1 to " + N + ". Same random starting heights for all 3 versions.");
        show(lists[0], names[0], "start");

        // ---- Phase 1: keys 5 and 12 are popular ----
        int[] hot1 = {5, 12};
        System.out.println("\n\n############ PHASE 1: 600 searches, keys 5 and 12 get 90% of them ############");
        double[] hops1 = playPhase(lists, hot1, 600, 11);
        for (int v = 0; v < 3; v++) show(lists[v], names[v], "after phase 1");
        printHops(hops1, 600);

        // ---- Phase 2: popularity moves to keys 2 and 14 ----
        int[] hot2 = {2, 14};
        System.out.println("\n\n############ PHASE 2: 1500 more searches, now keys 2 and 14 are popular ############");
        double[] hops2 = playPhase(lists, hot2, 1500, 22);
        for (int v = 1; v < 3; v++) show(lists[v], names[v], "after phase 2");
        printHops(hops2, 1500);
        System.out.println("\nLook at keys 5 and 12 in version 2: they keep their tall towers forever.");
        System.out.println("In version 3 the aging cut them down and gave the new popular keys (2 and 14) tall towers.");
    }

    // 90% of searches go to the two hot keys, 10% to a random key. All 3 lists get the same searches.
    static double[] playPhase(SkipList[] lists, int[] hot, int count, long seed) {
        Random r = new Random(seed);
        double[] hops = new double[3];
        for (int i = 0; i < count; i++) {
            int key = (r.nextInt(100) < 90) ? hot[r.nextInt(2)] : 1 + r.nextInt(N);
            for (int v = 0; v < 3; v++) hops[v] += lists[v].access(key);
        }
        return hops;
    }

    static void printHops(double[] hops, int count) {
        System.out.println("\nAverage hops per search in this phase:");
        System.out.printf("  Baseline %.1f    Counter %.1f    Counter+Aging %.1f%n", hops[0] / count, hops[1] / count, hops[2] / count);
    }

    // Prints the skip list three ways: a picture of the towers, the chain on each level, and a table.
    static void show(SkipList list, String title, String moment) {
        System.out.println("\n==================================================================");
        System.out.println(title + "   (" + moment + ")");
        System.out.println("==================================================================");

        // 1. picture of the towers
        System.out.println("Towers  ( o = normal tower,  # = tower made taller because the key is popular )");
        for (int lvl = list.level; lvl >= 1; lvl--) {
            StringBuilder sb = new StringBuilder(String.format("Level %2d  ", lvl));
            for (Node x = list.head.next[0]; x != null; x = x.next[0]) {
                String mark = "   .";
                if (x.height >= lvl) mark = (x.height > x.baseHeight && lvl > x.baseHeight) ? "   #" : "   o";
                sb.append(mark);
            }
            System.out.println(sb);
        }
        StringBuilder keys = new StringBuilder("Key       ");
        for (Node x = list.head.next[0]; x != null; x = x.next[0]) keys.append(String.format("%4d", x.key));
        System.out.println(keys);

        // 2. the actual linked chain on every level
        System.out.println("\nChain on each level (what the pointers really look like):");
        for (int lvl = list.level; lvl >= 1; lvl--) {
            StringBuilder sb = new StringBuilder(String.format("Level %2d: HEAD", lvl));
            for (Node x = list.head.next[lvl - 1]; x != null; x = x.next[lvl - 1]) sb.append(" -> ").append(x.key);
            sb.append(" -> null");
            System.out.println(sb);
        }

        // 3. table of elements
        if (list instanceof CounterSkipList || list instanceof AgingSkipList) {
            String hitsName = (list instanceof AgingSkipList) ? "recent hits (old ones fade)" : "hits so far";
            System.out.println("\nKey | random height | current height | " + hitsName);
            for (Node x = list.head.next[0]; x != null; x = x.next[0]) {
                double hits = x.count;
                if (list instanceof AgingSkipList) hits = x.count / ((AgingSkipList) list).weight;   // in "recent searches"
                String flag = x.height > x.baseHeight ? "  <-- raised" : "";
                System.out.printf("%3d |       %2d      |       %2d       | %8.1f%s%n", x.key, x.baseHeight, x.height, hits, flag);
            }
        } else {
            System.out.println("\nKey | height (never changes)");
            for (Node x = list.head.next[0]; x != null; x = x.next[0])
                System.out.printf("%3d | %2d%n", x.key, x.height);
        }
    }
}
