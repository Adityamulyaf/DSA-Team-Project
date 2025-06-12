import java.util.*;

public class TSP_Recursion {

    static int[][] memo;
    static int[][] bestPath;
    static String[] cityNames;

    static int INF = (int) 1e9;

    static int totalCost(int mask, int pos, int n, int[][] cost) {
        if (mask == (1 << n) - 1) {
            return cost[pos][0] == INF ? INF : cost[pos][0];
        }

        if (memo[pos][mask] != -1) return memo[pos][mask];

        int ans = INF;
        int nextCity = -1;

        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) == 0 && cost[pos][i] != INF) {
                int newMask = mask | (1 << i);
                int temp = cost[pos][i] + totalCost(newMask, i, n, cost);
                if (temp < ans) {
                    ans = temp;
                    nextCity = i;
                }
            }
        }

        memo[pos][mask] = ans;
        bestPath[pos][mask] = nextCity;
        return ans;
    }

    static List<Integer> reconstructPath(int n) {
        List<Integer> path = new ArrayList<>();
        int mask = 1;
        int pos = 0;
        path.add(pos);
        int steps = 0;

        while (true) {
            int next = bestPath[pos][mask];
            if (next == -1 || steps++ > n) break;
            path.add(next);
            mask |= (1 << next);
            pos = next;
        }

        path.add(0); // return to starting city
        return path;
    }

    public static void main(String[] args) {
        int INF = (int) 1e9;

        int[][] cost = {
            // PG,   RB,   RJ,   IS,   KS,   ISu,  TA
            {  0,   41,   74,   27, INF, INF, INF }, // Pasar Gede
            { 41,    0,   86, INF,  79, INF, INF }, // Rendra Buah
            { 74,   86,    0,  57, 110, 110,  91 }, // Raja Buah
            { 27, INF,   57,   0, INF, 112, INF },  // Istana Buah Srikandi
            {INF,  79,  110, INF,   0, INF,  82 },  // Kios Buah Segar
            {INF, INF,  110, 112, INF,   0, 183 },  // Istana Buah Sukoharjo
            {INF, INF,   91, INF,  82, 183,   0 }   // Toko Buah ABC Karanganyar
        };

        cityNames = new String[] {
            "Pasar Gede", "Rendra Buah", "Raja Buah",
            "Istana Buah Srikandi", "Kios Buah Segar",
            "Istana Buah Sukoharjo", "Toko Buah ABC Karanganyar"
        };

        int n = cost.length;
        memo = new int[n][1 << n];
        bestPath = new int[n][1 << n];
        for (int[] row : memo) Arrays.fill(row, -1);
        for (int[] row : bestPath) Arrays.fill(row, -1);

        int resultCost = totalCost(1, 0, n, cost);
        List<Integer> route = reconstructPath(n);

        System.out.println("Total cost: " + resultCost);
        System.out.println("Route taken (index): " + route);
        System.out.print("Route taken (named): ");
        for (int i = 0; i < route.size(); i++) {
            System.out.print(cityNames[route.get(i)]);
            if (i != route.size() - 1) System.out.print(" → ");
        }

        System.out.println("\n\nDetail perjalanan:");
        for (int i = 0; i < route.size() - 1; i++) {
            int a = route.get(i);
            int b = route.get(i + 1);
            System.out.println(cityNames[a] + " - " + cost[a][b] + " km - " + cityNames[b]);
        }
    }
}
