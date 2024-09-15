package com.adista.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.adista.finalproject.adapter.FriendAdapter
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FriendAdapter.OnFriendClickListener {
    private lateinit var binding: ActivityMainBinding
    private val friendViewModel: FriendViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        handlePermissionResults(permissions)
    }

    private lateinit var adapter: FriendAdapter // Declare adapter variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }

        adapter = FriendAdapter(this, emptyList(), this) // Initialize the adapter

        binding.rvShowData.adapter = adapter

        friendViewModel.getAllFriends().observe(this) { friends ->
            adapter.updateData(friends)
        }


        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    adapter.updateData(adapter.getData()) // Tampilkan semua data asli
                    binding.ivNotFound.visibility = View.GONE
                    binding.rvShowData.visibility = View.VISIBLE
                } else {
                    filterFriends(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun checkAndRequestPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.CAMERA) // No need for READ_EXTERNAL_STORAGE on Android 10+
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

        val deniedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }


    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value } // Mencari izin yang ditolak

        if (deniedPermissions.isEmpty()) {
            Toast.makeText(this, "Semua izin diberikan.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Beberapa izin ditolak: ${deniedPermissions.keys.joinToString()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterFriends(query: String) {
        Log.d("MainActivity", "filterFriends: query = $query") // Tambahkan log debug

        val filteredList = if (query.isEmpty()) {
            adapter.getData() // Jika pencarian kosong, ambil data penuh
        } else {
            adapter.getFilteredList(query) // Gunakan getFilteredList untuk filter
        }

        Log.d("MainActivity", "filterFriends: filteredList.size = ${filteredList.size}") // Tambahkan log debug


        // Memeriksa apakah daftar teman hasil pencarian kosong
        if (filteredList.isEmpty()) {
            // Jika tidak ada hasil pencarian, tampilkan gambar atau pesan "Pencarian tidak ditemukan"
            binding.ivNotFound.visibility = View.VISIBLE
            binding.rvShowData.visibility = View.GONE
            Toast.makeText(this, "Friend Not Found", Toast.LENGTH_SHORT).show()
        } else {
            // Jika ada hasil pencarian, perbarui data adapter dengan hasil filter
            binding.ivNotFound.visibility = View.GONE
            binding.rvShowData.visibility = View.VISIBLE
            adapter.updateData(filteredList)
        }
    }

    override fun onFriendClick(friendId: Int) {
        // Arahkan ke DetailFriendActivity dan kirimkan ID teman
        val intent = Intent(this, DetailFriendActivity::class.java)
        intent.putExtra("FRIEND_ID", friendId) // Mengirim ID teman ke DetailFriendActivity
        startActivity(intent)
    }
}
