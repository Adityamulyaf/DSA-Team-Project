import java.util.*;

public class TokoBuah_HeldKarp {

    static final int N = 7;
    static final double INF = Double.POSITIVE_INFINITY;

    static double[][] jarak = {
        {0.0, 4.1, 7.4, INF, INF, 2.7, INF},
        {4.1, 0.0, 8.6, 7.9, INF, INF, INF},
        {7.4, 8.6, 0.0, 11.0, 9.1, 5.7, 11.0},
        {INF, 7.9, 11.0, 0.0, 8.2, INF, INF},
        {INF, INF, 9.1, 8.2, 0.0, INF, 18.3},
        {2.7, INF, 5.7, INF, INF, 0.0, 11.2},
        {INF, INF, 11.0, INF, 18.3, 11.2, 0.0}
    };

    static String[] toko = {
        "Pasar Gede",
        "Rendra Buah",
        "Raja Buah",
        "Kios Buah Segar",
        "Toko Buah ABC",
        "Istana Buah Srikandi",
        "Istana Buah Sukoharjo"
    };

    public static void main(String[] args) {
        int size = 1 << N;
        double[][] dp = new double[size][N];
        int[][] parent = new int[size][N];

        for (int i = 0; i < size; i++) {
            Arrays.fill(dp[i], INF);
        }

        for (int i = 1; i < N; i++) {
            dp[1 << i | 1][i] = jarak[0][i];
            parent[1 << i | 1][i] = 0;
        }

        for (int mask = 0; mask < size; mask++) {
            for (int u = 0; u < N; u++) {
                if ((mask & (1 << u)) == 0) continue; 
                for (int v = 0; v < N; v++) {
                    if ((mask & (1 << v)) != 0) continue; 
                    int nextMask = mask | (1 << v);
                    double newDist = dp[mask][u] + jarak[u][v];
                    if (newDist < dp[nextMask][v]) {
                        dp[nextMask][v] = newDist;
                        parent[nextMask][v] = u;
                    }
                }
            }
        }

        double minCost = INF;
        int last = -1;
        int fullMask = (1 << N) - 1;

        for (int i = 1; i < N; i++) {
            double total = dp[fullMask][i] + jarak[i][0];
            if (total < minCost) {
                minCost = total;
                last = i;
            }
        }

        List<Integer> path = new ArrayList<>();
        int mask = fullMask;
        while (last != 0) {
            path.add(last);
            int temp = parent[mask][last];
            mask = mask ^ (1 << last);
            last = temp;
        }
        Collections.reverse(path);

        System.out.println("Rute terpendek (Held-Karp):");
        System.out.print(toko[0] + " -> ");
        for (int tokoIndex : path) {
            System.out.print(toko[tokoIndex] + " -> ");
        }
        System.out.println(toko[0]);
        System.out.println("Total jarak: " + minCost);
    }
}
