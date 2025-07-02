import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class BFS {

    // Fungsi pencarian file menggunakan algoritma BFS
    public static void search(
            String rootDirPath,                         // path direktori root tempat pencarian dimulai
            String targetFilePattern,                   // nama file atau pola nama file yang dicari (bisa pakai wildcard *)
            boolean findAll,                            // jika true, cari semua file yang cocok, jika false berhenti saat ketemu pertama
            Map<String, TreeNodeData> nodeMap,           // struktur pohon folder/file yang sudah dibuat sebelumnya
            Set<String> visitedPaths,                    // set untuk menyimpan path yang sudah dikunjungi (untuk visualisasi)
            List<String> traversalOrder,                 // list urutan traversal (untuk visualisasi atau log)
            Set<String> foundFiles,                      // set hasil file yang ditemukan sesuai pola
            Consumer<String> statusUpdater,              // fungsi callback untuk update status GUI/console
            long searchStartTime                         // waktu pencarian dimulai (untuk hitung durasi)
    ) throws InterruptedException {

        // Inisialisasi queue BFS, isinya objek File
        Queue<File> queue = new LinkedList<>();
        // Masukkan direktori root ke antrian
        queue.offer(new File(rootDirPath));

        // Selama queue tidak kosong, lakukan iterasi
        while (!queue.isEmpty()) {
            
            // Jika tidak perlu mencari semua file dan sudah ada yang ketemu, hentikan
            if (!findAll && !foundFiles.isEmpty()) {
                break;
            }

            // Ambil file/direktori paling depan dari queue
            File current = queue.poll();
            String currentPath = current.getAbsolutePath(); // .getAbsolutePath itu buat mendapatkan path lengkap

            // Cek apakah path saat ini ada dalam nodeMap (hanya jalur yang diizinkan)
            if (!nodeMap.containsKey(currentPath)) {
                continue;
            }

            // Tandai path saat ini sebagai sudah dikunjungi
            visitedPaths.add(currentPath);
            // Simpan urutan traversal
            traversalOrder.add(currentPath);

            // Update status GUI/console via statusUpdater
            statusUpdater.accept("Searching (BFS): " + current.getName());

            // Jika file, cek apakah cocok dengan pattern yang dicari
            if (current.isFile()) {
                if (matchesPattern(current.getName(), targetFilePattern)) {
                    // Jika cocok, tambahkan ke hasil foundFiles
                    foundFiles.add(currentPath);
                }
            }
            // Jika direktori, tambahkan semua anak-anaknya ke queue
            else if (current.isDirectory()) {
                File[] children = current.listFiles();
                if (children != null) {
                    // Urutkan: folder dulu, baru file, lalu berdasarkan nama (alfabet)
                    Arrays.sort(children, (a, b) -> {
                        if (a.isDirectory() && !b.isDirectory()) return -1;
                        if (!a.isDirectory() && b.isDirectory()) return 1;
                        return a.getName().compareToIgnoreCase(b.getName());
                    });

                    // Masukkan semua anak ke queue jika ada dalam nodeMap
                    for (File child : children) {
                        if (nodeMap.containsKey(child.getAbsolutePath())) {
                            queue.offer(child);
                        }
                    }
                }
            }

            // Beri jeda agar traversal tidak terlalu cepat (bisa untuk simulasi/visualisasi)
            Thread.sleep(50);
        }
    }

    // Fungsi untuk mencocokkan nama file dengan pola wildcard
    private static boolean matchesPattern(String fileName, String pattern) {
        // Jika pattern mengandung wildcard '*', ubah jadi regex
        if (pattern.contains("*")) {
            // Escape karakter titik, lalu ganti * menjadi .*
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            // Cek apakah nama file cocok dengan regex, case-insensitive
            return fileName.matches("(?i)" + regex);
        }
        // Jika tidak ada wildcard, lakukan perbandingan nama biasa (case-insensitive)
        return fileName.equalsIgnoreCase(pattern);
    }
}
