package reference;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class referenceBFS {

    // Fungsi BFS: menjelajah folder satu level demi satu level
    public static void bfs(File root) {
        // Buat queue untuk BFS
        Queue<File> queue = new LinkedList<>();
        // Masukkan direktori root ke queue
        queue.offer(root); // offer = menambahkan elemen ke queue

        // Selama queue tidak kosong
        while (!queue.isEmpty()) {
            // Ambil file/direktori paling depan dari queue
            File current = queue.poll(); // poll = elemen paling depam diambil
            // Tampilkan path yang sedang dikunjungi
            System.out.println("Visited: " + current.getAbsolutePath());

            // Jika current adalah direktori, masukkan anak-anaknya ke queue
            if (current.isDirectory()) {
                File[] children = current.listFiles();
                if (children != null) {
                    for (File child : children) {
                        // Masukkan anak ke queue
                        queue.offer(child);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // Tentukan direktori root untuk BFS
        File rootDir = new File("C:/contoh/folder"); // ganti sesuai path lokal
        // Panggil fungsi BFS
        bfs(rootDir);
    }
}

// modifikasi:
// 1. menyimpan urutan traversal
// 2. memberikan jeda waktu untuk visualisasi
// 3. menggunakan nodeMap sebagai batasan node mana saja yang boleh diakses
// 4. menerapkan fitur pencarian pattern dengan wildcard
// 5. mengatur opsi untuk berhenti saat file pertama ditemukan atau lanjut mencari semua file