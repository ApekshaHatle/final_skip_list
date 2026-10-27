import java.util.Random;

public class Workloads {

    static int[] make(String name, int n, int m, long seed, int shiftEvery) {
        Random r = new Random(seed);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        shuffle(perm, r);
        int[] out = new int[m];

        if (name.equals("uniform")) {
            for (int i = 0; i < m; i++) out[i] = perm[r.nextInt(n)];
        } else if (name.equals("zipf")) {
            double[] cdf = new double[n];
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += 1.0 / (i + 1);
                cdf[i] = sum;
            }
            for (int i = 0; i < m; i++) {
                double u = r.nextDouble() * sum;
                int lo = 0, hi = n - 1;
                while (lo < hi) {
                    int mid = (lo + hi) / 2;
                    if (cdf[mid] < u) lo = mid + 1; else hi = mid;
                }
                out[i] = perm[lo];
            }
        } else {
            boolean shifting = name.startsWith("shift");
            String[] p = name.substring(shifting ? 5 : 3).split("_");
            int hotShare = Integer.parseInt(p[0]);
            int hotKeys = n * Integer.parseInt(p[1]) / 100;
            for (int i = 0; i < m; i++) {
                if (shifting && i > 0 && i % shiftEvery == 0) shuffle(perm, r);
                int idx;
                if (r.nextInt(100) < hotShare) idx = r.nextInt(hotKeys);
                else idx = hotKeys + r.nextInt(n - hotKeys);
                out[i] = perm[idx];
            }
        }
        return out;
    }

    static void shuffle(int[] a, Random r) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }
}
