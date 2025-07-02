package reference;
import java.util.*;

public class pseudocode_pure_BFS {
    static final int MAX = 20;
    static boolean[] dikunjungi = new boolean[MAX];
    static int[][] adj = new int[MAX][MAX]; // Matriks ketetanggaan
    static int n; // Jumlah simpul

    static void BFS(int v) {
        Queue<Integer> q = new LinkedList<>();
        
        System.out.print(v + " ");
        dikunjungi[v] = true;
        q.add(v);

        while (!q.isEmpty()) {
            v = q.poll();

            for (int w = 1; w <= n; w++) {
                if (adj[v][w] == 1 && !dikunjungi[w]) {
                    System.out.print(w + " ");
                    q.add(w);
                    dikunjungi[w] = true;
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Jumlah simpul: ");
        n = input.nextInt();

        System.out.println("Masukkan matriks ketetanggaan:");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                adj[i][j] = input.nextInt();
            }
        }

        System.out.print("Masukkan simpul awal: ");
        int start = input.nextInt();

        System.out.println("Hasil traversal BFS:");
        BFS(start);
    }
}