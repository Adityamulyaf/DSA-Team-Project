import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DFS {

    // Fungsi pencarian file menggunakan algoritma DFS
    public static boolean search(
            File current,                                  // File atau direktori saat ini yang sedang diproses
            String targetFilePattern,                       // Pola nama file yang dicari (bisa wildcard *)
            boolean findAll,                                // Jika true, cari semua file. Jika false, stop di file pertama ketemu
            Map<String, TreeNodeData> nodeMap,               // Struktur tree yang sudah dibangun sebelumnya (untuk simulasi visualisasi)
            Set<String> visitedPaths,                       // Menyimpan path yang sudah dikunjungi
            List<String> traversalOrder,                    // Menyimpan urutan traversal (untuk log atau visualisasi)
            Set<String> foundFiles,                         // Menyimpan hasil file yang ditemukan
            Consumer<String> statusUpdater,                 // Callback untuk update status (misal ke GUI)
            long searchStartTime                            // Waktu pencarian dimulai (untuk menghitung durasi)
    ) throws InterruptedException {

        // Kondisi berhenti jika mode findAll false dan file sudah ditemukan
        if (!findAll && !foundFiles.isEmpty()) {
            return true; // Mengembalikan sinyal agar recursive parent-nya bisa langsung stop
        }

        String currentPath = current.getAbsolutePath();

        // Hanya proses node jika path-nya terdaftar di nodeMap (untuk batasi traversal)
        if (!nodeMap.containsKey(currentPath)) {
            return false;
        }

        // Tandai path ini sebagai sudah dikunjungi
        visitedPaths.add(currentPath);
        // Simpan urutan traversal-nya
        traversalOrder.add(currentPath);

        // Update status ke GUI/console melalui statusUpdater
        statusUpdater.accept("Searching (DFS): " + current.getName());

        // Jika file, cek apakah cocok dengan pola target
        if (current.isFile()) {
            if (matchesPattern(current.getName(), targetFilePattern)) {
                // Jika cocok, masukkan ke foundFiles
                foundFiles.add(currentPath);
                // Jika mode bukan findAll, hentikan pencarian setelah ketemu pertama
                if (!findAll) {
                    return true; // Kirim sinyal stop ke atas
                }
            }
        }
        // Jika direktori, lakukan recursive DFS ke setiap child-nya
        else if (current.isDirectory()) {
            File[] children = current.listFiles();
            if (children != null) {
                // Urutkan child: folder dulu, lalu file, urut alfabet
                Arrays.sort(children, (a, b) -> {
                    if (a.isDirectory() && !b.isDirectory()) return -1;
                    if (!a.isDirectory() && b.isDirectory()) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                });

                // Lakukan DFS untuk setiap child yang ada di nodeMap
                for (File child : children) {
                    if (nodeMap.containsKey(child.getAbsolutePath())) {
                        // Jika recursive DFS menemukan file target, berhenti jika mode findAll = false
                        if (search(child, targetFilePattern, findAll, nodeMap, visitedPaths, traversalOrder, foundFiles, statusUpdater, searchStartTime)) {
                            if (!findAll) {
                                return true; // Propagasi sinyal stop ke recursive atas
                            }
                        }
                    }
                }
            }
        }

        // Delay untuk simulasi visualisasi traversal agar tidak terlalu cepat
        Thread.sleep(50);
        return false; // Jika tidak ditemukan di path ini, kembali false
    }

    // Fungsi pencocokan nama file dengan pola (mendukung wildcard *)
    private static boolean matchesPattern(String fileName, String pattern) {
        // Jika pattern mengandung wildcard '*', ubah jadi regex
        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            // Cek kecocokan fileName dengan regex, case-insensitive
            return fileName.matches("(?i)" + regex);
        }
        // Jika tidak, bandingkan nama file secara langsung (case-insensitive)
        return fileName.equalsIgnoreCase(pattern);
    }
}