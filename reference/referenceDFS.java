package reference;
import java.io.File;

public class referenceDFS {

    // Fungsi DFS: masuk ke folder terdalam dulu sebelum lanjut ke yang lain.
    public static void dfs(File current) {
        // Tampilkan path yang sedang dikunjungi
        System.out.println("Visited: " + current.getAbsolutePath());

        // Jika current adalah direktori, lakukan rekursi ke anak-anaknya
        if (current.isDirectory()) {
            File[] children = current.listFiles();
            if (children != null) {
                for (File child : children) {
                    // Rekursif panggil DFS ke child
                    dfs(child);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Tentukan direktori root untuk DFS
        File rootDir = new File("C:/contoh/folder"); // ganti sesuai path lokal
        // Panggil fungsi DFS
        dfs(rootDir);
    }
}

// modifikasi:
// 1. menyimpan urutan traversal
// 2. memberikan jeda waktu untuk visualisasi
// 3. menggunakan nodeMap sebagai batasan node mana saja yang boleh diakses
// 4. menerapkan fitur pencarian pattern dengan wildcard
// 5. mengatur opsi untuk berhenti saat file pertama ditemukan atau lanjut mencari semua file