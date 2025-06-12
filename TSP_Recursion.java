import java.util.*;

public class TSP_Recursion {

    static double[][] memo;
    static int[][] bestPath;
    static String[] cityNames;

    static double INF = Double.POSITIVE_INFINITY;

    static double totalCost(int mask, int pos, int n, double[][] cost) {
        if (mask == (1 << n) - 1) {
            return cost[pos][0] == INF ? INF : cost[pos][0];
        }

        if (memo[pos][mask] != -1) return memo[pos][mask];

        double ans = INF;
        int nextCity = -1;

        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) == 0 && cost[pos][i] != INF) {
                int newMask = mask | (1 << i);
                double temp = cost[pos][i] + totalCost(newMask, i, n, cost);
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
        double INF = Double.POSITIVE_INFINITY;

        double[][] cost = {
            // PG,   RB,   RJ,   IS,   KS,   ISu,   TA
            {  0, 4.1, 7.4, 2.7, INF, INF, INF },   //y Pasar Gede
            {4.1,   0, 8.6, INF, 7.9, INF, INF },   //y Rendra Buah
            {7.4, 8.6,   0, 5.7, 11.0, 11.0, 9.1 }, //y Raja Buah
            {2.7, INF, 5.7,   0, INF, 11.2, INF },  //y Istana Buah Srikandi
            {INF, 7.9, 11.0, INF, 0, INF, 8.2 },    //y Kios Buah Segar
            {INF, INF, 11.0, 11.2, INF, 0, 18.3 },  //y Istana Buah Sukoharjo
            {INF, INF, 9.1, INF, 8.2, 18.3, 0 }     //y Toko Buah ABC Karanganyar
        };

        cityNames = new String[] {
            "Pasar Gede", "Rendra Buah", "Raja Buah",
            "Istana Buah Srikandi", "Kios Buah Segar",
            "Istana Buah Sukoharjo", "Toko Buah ABC Karanganyar"
        };

        int n = cost.length;
        memo = new double[n][1 << n];
        bestPath = new int[n][1 << n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(memo[i], -1);
            Arrays.fill(bestPath[i], -1);
        }

        double resultCost = totalCost(1, 0, n, cost);
        List<Integer> route = reconstructPath(n);

        System.out.printf("Total cost: %.2f km\n", resultCost);
        System.out.println("Route taken (index): " + route);
        System.out.print("Route taken (named): ");
        for (int i = 0; i < route.size(); i++) {
            System.out.print(cityNames[route.get(i)]);
            if (i != route.size() - 1) System.out.print(" - ");
        }

        System.out.println("\n\nDetail perjalanan:");
        for (int i = 0; i < route.size() - 1; i++) {
            int a = route.get(i);
            int b = route.get(i + 1);
            System.out.printf("%s - %.2f km - %s\n", cityNames[a], cost[a][b], cityNames[b]);
        }
    }
}
