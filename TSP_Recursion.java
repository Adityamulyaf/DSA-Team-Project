// Java program to find the shortest possible route
// that visits every city exactly once and returns to
// the starting point using memoization and bitmasking

import java.util.*;

public class TSP_Recursion {

    static int[][] memo;
    static int[][] bestPath;
    static String[] cityNames;

    // Recursive TSP with memoization and path tracking
    static int totalCost(int mask, int pos, int n, int[][] cost) {
        // Base case: all cities visited, return to start
        if (mask == (1 << n) - 1) {
            return cost[pos][0];
        }

        if (memo[pos][mask] != -1) return memo[pos][mask];

        int ans = Integer.MAX_VALUE;
        int nextCity = -1;

        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) == 0) {
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

    // Reconstruct the route from bestPath table
    static List<Integer> reconstructPath(int n) {
        List<Integer> path = new ArrayList<>();
        int mask = 1;
        int pos = 0;
        path.add(pos);
        int steps = 0;

        while (true) {
            int next = bestPath[pos][mask];
            if (next == -1 || steps++ > n) break; // prevent infinite loop
            path.add(next);
            mask |= (1 << next);
            pos = next;
        }

        path.add(0); // return to starting city
        return path;
    }

    public static void main(String[] args) {
        int[][] cost = {
            { 0, 10, 100, 20, 56 },
            { 10, 0, 35, 25, 21 },
            { 100, 35, 0, 30, 12 },
            { 20, 25, 30, 0, 12 },
            { 56, 21, 12, 12, 0 }
        };

        cityNames = new String[] {
            "Toko A", "Toko B", "Toko C", "Toko D", "Toko E"
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
