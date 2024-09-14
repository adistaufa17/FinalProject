package com.adista.finalproject

// Import library yang dibutuhkan untuk Intent, WindowInsets, dan ViewBinding
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adista.finalproject.databinding.ActivityNotFoundBinding

// Kelas NotFoundActivity adalah activity yang menampilkan halaman "Not Found" jika sesuatu tidak ditemukan
class NotFoundActivity : AppCompatActivity() {

    // Variabel untuk binding layout dari XML
    private lateinit var binding: ActivityNotFoundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan ViewBinding untuk menghubungkan tampilan dari XML ke activity ini
        binding = ActivityNotFoundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengaktifkan fitur edge-to-edge pada tampilan
        enableEdgeToEdge()

        // Mengatur padding pada tampilan untuk menyesuaikan dengan insets (misalnya, status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            // Mendapatkan ukuran dari system bars (seperti status bar dan navigation bar)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Mengatur padding pada tampilan root sesuai dengan ukuran system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        // Listener untuk tombol "Back" yang akan membawa pengguna kembali ke MainActivity
        binding.btnBack.setOnClickListener {
            // Membuat intent untuk berpindah ke MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // Memulai activity MainActivity
            startActivity(intent)

            // Menutup NotFoundActivity setelah berpindah
            finish()
        }
    }
}