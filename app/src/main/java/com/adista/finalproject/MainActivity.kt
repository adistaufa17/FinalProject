package com.adista.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.adista.finalproject.database.FriendAdapter
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FriendAdapter.OnFriendClickListener {

    // Variabel untuk binding tampilan activity
    private lateinit var binding: ActivityMainBinding

    // ViewModel untuk mengambil data teman dari database
    private val friendViewModel: FriendViewModel by viewModels()

    // Pendaftaran launcher untuk meminta beberapa izin sekaligus
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        handlePermissionResults(permissions) // Memanggil fungsi untuk menangani hasil izin
    }

    // Variabel adapter untuk RecyclerView
    private lateinit var adapter: FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memeriksa dan meminta izin yang diperlukan
        checkAndRequestPermissions()

        // Mengatur tindakan ketika tombol "Add" ditekan
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Memberikan izin baca URI gambar
            startActivity(intent) // Memulai AddFriendActivity
        }

        // Inisialisasi adapter dengan data kosong
        adapter = FriendAdapter(this, emptyList(), this)

        // Mengatur adapter pada RecyclerView
        binding.rvShowData.adapter = adapter

        // Mengamati perubahan data teman dari ViewModel
        friendViewModel.getAllFriends().observe(this) { friends ->
            adapter.updateData(friends) // Memperbarui data pada adapter saat ada perubahan
        }

        // Menambahkan listener untuk kolom pencarian (search bar)
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString()) // Memfilter teman berdasarkan input pencarian
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Fungsi untuk memeriksa dan meminta izin yang diperlukan (baca penyimpanan dan kamera)
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        // Filter izin yang belum diberikan oleh pengguna
        val deniedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        // Jika ada izin yang ditolak, minta izin tersebut
        if (deniedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }

    // Fungsi untuk menangani hasil dari permintaan izin
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val allGranted = permissions.all { it.value } // Memeriksa apakah semua izin diberikan

        if (allGranted) {
            Toast.makeText(this, "Semua izin diberikan. Sekarang Anda bisa mengakses gambar.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Beberapa izin ditolak. Tidak bisa mengakses gambar.", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk memfilter data teman berdasarkan input pencarian
    private fun filterFriends(query: String) {
        val filteredList = if (query.isEmpty()) {
            adapter.getData() // Jika pencarian kosong, ambil data penuh
        } else {
            adapter.getFilteredList(query) // Jika ada input, ambil daftar teman yang difilter
        }

        // Memeriksa apakah daftar teman hasil pencarian kosong
        if (filteredList.isEmpty()) {
            // Jika tidak ada hasil pencarian, buka NotFoundActivity
            val intent = Intent(this, NotFoundActivity::class.java)
            startActivity(intent)
        } else {
            // Jika ada hasil pencarian, perbarui data adapter dengan hasil filter
            adapter.updateData(filteredList)
        }
    }

    // Fungsi yang dipanggil ketika item teman di klik pada RecyclerView
    override fun onFriendClick(friendId: Int) {
        // Membuka DetailFriendActivity dan mengirimkan ID teman yang dipilih
        val intent = Intent(this, DetailFriendActivity::class.java)
        intent.putExtra("FRIEND_ID", friendId) // Mengirim ID teman ke DetailFriendActivity
        startActivity(intent) // Memulai DetailFriendActivity
    }
}
