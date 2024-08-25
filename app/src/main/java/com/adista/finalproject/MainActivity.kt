package com.adista.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adista.finalproject.database.FriendAdapter
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val permisionReqCD = 100
    private lateinit var binding: ActivityMainBinding
    private val friendViewModel: FriendViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            // Izin diberikan
        } else {
            Toast.makeText(this, "Permission denied. Unable to access images.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request permissions
        checkPermissions()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAdd.setOnClickListener {
            // Intent untuk pindah ke AddFriendActivity dengan izin URI
            val intent = Intent(this, AddFriendActivity::class.java)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Berikan izin membaca URI
            startActivity(intent)
        }

        // Set up RecyclerView
        val adapter = FriendAdapter(emptyList())
        binding.rvShowData.adapter = adapter

        // Observe data changes
        friendViewModel.getAllFriends().observe(this) { friends ->
            adapter.updateData(friends)  // Modify adapter to use updateData method
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        val deniedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(arrayOf(deniedPermissions.first()))
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permisionReqCD) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. You can now load images.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied. Unable to access images.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
