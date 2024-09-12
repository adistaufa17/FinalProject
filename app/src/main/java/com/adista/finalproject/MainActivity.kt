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
                filterFriends(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        val deniedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }

    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val allGranted = permissions.all { it.value }

        if (allGranted) {
            Toast.makeText(this, "Permissions granted. You can now access images.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied. Unable to access images.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterFriends(query: String) {
        val filteredList = if (query.isEmpty()) {
            adapter.getData() // Get data from the adapter
        } else {
            adapter.getFilteredList(query) // Get filtered list from the adapter
        }

        // Check if the filtered list is empty
        if (filteredList.isEmpty()) {
            // Launch NotFoundActivity when no results are found
            val intent = Intent(this, NotFoundActivity::class.java)
            startActivity(intent)
        } else {
            // Update the adapter with the filtered list if there are results
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
