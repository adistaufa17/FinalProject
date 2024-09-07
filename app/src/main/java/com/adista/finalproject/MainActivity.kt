package com.adista.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.adista.finalproject.database.FriendAdapter
import com.adista.finalproject.database.FriendDatabase
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val friendViewModel: FriendViewModel by viewModels()

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        handlePermissionResults(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request necessary permissions
        checkAndRequestPermissions()

        // Set up the button to navigate to AddFriendActivity
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }

        // Set up the RecyclerView
        val adapter = FriendAdapter(this, emptyList())
        binding.rvShowData.adapter = adapter

        // Observe LiveData from ViewModel to update RecyclerView
        friendViewModel.getAllFriends().observe(this) { friends ->
            adapter.updateData(friends)
        }
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


}
