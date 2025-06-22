import java.util.ArrayList;
import java.util.Collections;

public class TokoBuah_TSP_BnB {

    static double[][] jarak = {
        {0.0, 4.1, 7.4, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2.7, Double.POSITIVE_INFINITY},
        {4.1, 0.0, 8.6, 7.9, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {7.4, 8.6, 0.0, 11.0, 9.1, 5.7, 11.0},
        {Double.POSITIVE_INFINITY, 7.9, 11.0, 0.0, 8.2, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 9.1, 8.2, 0.0, Double.POSITIVE_INFINITY, 18.3},
        {2.7, Double.POSITIVE_INFINITY, 5.7, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, 11.2},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 11.0, Double.POSITIVE_INFINITY, 18.3, 11.2, 0.0}
    };

    static String[] namaToko = {
        "Pasar Gede",
        "Rendra Buah",
        "Raja Buah",
        "Kios Buah Segar",
        "Toko Buah ABC",
        "Istana Buah Srikandi",
        "Istana Buah Sukoharjo"
    };

    static double jarakMinimum = Double.MAX_VALUE;
    static ArrayList<Integer> ruteTerpendek = new ArrayList<>();

    public static void main(String[] args) {
        ArrayList<Integer> toko = new ArrayList<>();
        for (int i = 1; i < jarak.length; i++) {
            toko.add(i);
        }

        permutasiBnb(toko, 0, 0.0);

        System.out.println("\nRute terpendek (Branch and Bound):");
        System.out.print(namaToko[0] + " -> ");
        for (int t : ruteTerpendek) {
            System.out.print(namaToko[t] + " -> ");
        }
        System.out.println(namaToko[0]);
        System.out.println("Total jarak: " + jarakMinimum);
    }

    static void permutasiBnb(ArrayList<Integer> data, int index, double costSoFar) {
        if (costSoFar >= jarakMinimum) return; 

        if (index == data.size()) {
            double total = jarak[0][data.get(0)];
            for (int i = 0; i < data.size() - 1; i++) {
                total += jarak[data.get(i)][data.get(i + 1)];
            }
            total += jarak[data.get(data.size() - 1)][0];

            if (total < jarakMinimum) {
                jarakMinimum = total;
                ruteTerpendek = new ArrayList<>(data);
            }
            return;
        }

        for (int i = index; i < data.size(); i++) {
            Collections.swap(data, i, index);
            double tambahan = (index == 0) ? jarak[0][data.get(index)]
                                           : jarak[data.get(index - 1)][data.get(index)];
            permutasiBnb(data, index + 1, costSoFar + tambahan);
            Collections.swap(data, i, index);
        }
    }
}
