// Compares the 3 versions on the same searches. Prints a simple table.
// Usage: java Experiment [firstSeed] [seedCount] [halfLife]
public class Experiment {
    static final int N = 100000;

    static SkipList make(int version, long seed, double halfLife) {
        SkipList list;
        if (version == 1) list = new SkipList(seed);
        else if (version == 2) list = new CounterSkipList(seed);
        else list = new AgingSkipList(seed, halfLife);
        for (int k = 0; k < N; k++) list.insert(k);
        return list;
    }

    // returns {average hops, tower changes per 1000 searches}
    static double[] run(SkipList list, int[] searches) {
        return run(list, searches, null);
    }

    // blocks (optional): average hops for every block of 1,000,000 searches
    static double[] run(SkipList list, int[] searches, double[] blocks) {
        long total = 0, blockSum = 0;
        for (int i = 0; i < searches.length; i++) {
            int h = list.access(searches[i]);
            total += h;
            blockSum += h;
            if (blocks != null && (i + 1) % 1000000 == 0) {
                blocks[(i + 1) / 1000000 - 1] = blockSum / 1000000.0;
                blockSum = 0;
            }
        }
        if (!list.check()) throw new RuntimeException("skip list is broken!");
        double hops = (double) total / searches.length;
        double moves = 1000.0 * (list.promotions + list.demotions) / searches.length;
        return new double[]{hops, moves};
    }

    public static void main(String[] args) {
        long firstSeed = args.length > 0 ? Long.parseLong(args[0]) : 21;
        int seeds = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        double halfLife = args.length > 2 ? Double.parseDouble(args[2]) : 100000;
        String[] names = {"uniform", "hot90_10", "hot95_5", "hot99_1", "zipf", "shift99_1", "shift95_5"};
        String[] pretty = {"Uniform", "90-10", "95-5", "99-1", "Zipf", "Shifting 99-1", "Shifting 95-5"};

        System.out.println("Skip list with " + N + " keys, " + seeds + " seeds, half-life " + (long) halfLife);
        System.out.println("Average number of hops per search (lower is better)");
        System.out.println();
        System.out.printf("%-15s %9s %9s %9s | %13s %13s%n", "Workload", "Baseline", "Counter", "Aging",
                "Aging vs Base", "Aging vs Cnt");
        double[][] blockAvg = new double[3][4];     // for the shifting 99-1 table
        System.out.println("---------------------------------------------------------------------------------");

        for (int w = 0; w < names.length; w++) {
            boolean shifting = names[w].startsWith("shift");
            int m = shifting ? 4000000 : 2000000;
            double[] sumHops = new double[3];
            double[] vsBase = new double[seeds], vsCnt = new double[seeds];
            for (int s = 0; s < seeds; s++) {
                long seed = firstSeed + s;
                int[] searches = Workloads.make(names[w], N, m, seed, 1000000);
                double[] h = new double[3];
                for (int v = 1; v <= 3; v++) {
                    double[] blocks = new double[m / 1000000];
                    h[v - 1] = run(make(v, seed, halfLife), searches, blocks)[0];
                    if (names[w].equals("shift99_1"))
                        for (int b = 0; b < 4; b++) blockAvg[v - 1][b] += blocks[b] / seeds;
                    sumHops[v - 1] += h[v - 1] / seeds;
                }
                vsBase[s] = 100 * (h[2] / h[0] - 1);
                vsCnt[s] = 100 * (h[2] / h[1] - 1);
            }
            System.out.printf("%-15s %9.2f %9.2f %9.2f | %+12.1f%% %+12.1f%%%n", pretty[w],
                    sumHops[0], sumHops[1], sumHops[2], mean(vsBase), mean(vsCnt));
        }
        printBlocks(blockAvg);
    }

    static void printBlocks(double[][] b) {
        System.out.println();
        System.out.println("Shifting 99-1: hops in each block of 1,000,000 searches");
        System.out.println("(the popular keys change at the start of blocks 2, 3 and 4)");
        System.out.printf("%-10s %9s %9s %9s %9s%n", "", "Block 1", "Block 2", "Block 3", "Block 4");
        String[] n = {"Baseline", "Counter", "Aging"};
        for (int v = 0; v < 3; v++)
            System.out.printf("%-10s %9.2f %9.2f %9.2f %9.2f%n", n[v], b[v][0], b[v][1], b[v][2], b[v][3]);
    }

    static double mean(double[] a) {
        double s = 0;
        for (double x : a) s += x;
        return s / a.length;
    }
}
