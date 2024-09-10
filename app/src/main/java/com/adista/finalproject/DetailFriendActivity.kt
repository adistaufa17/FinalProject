package com.adista.finalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityDetailFriendBinding

class DetailFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFriendBinding

    // ViewModel untuk mengambil data friend
    private val friendViewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge for the activity
        enableEdgeToEdge()

        // Apply window insets to the layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listener for the edit button
        binding.btnEdit.setOnClickListener {
            val destination = Intent(this, EditFriendActivity::class.java)
            startActivity(destination)
        }

        // Retrieve friendId from Intent
        val friendId = intent.getIntExtra("FRIEND_ID", -1)
        if (friendId == -1) {
            finish() // Close activity if ID is not valid
            return
        }

        // Load friend details using friendId
        loadFriendDetails(friendId)
    }

    private fun loadFriendDetails(friendId: Int) {
        // Observe the friend from the ViewModel
        friendViewModel.getFriendById(friendId).observe(this) { friend ->
            if (friend != null) {
                // Update UI with friend details
                bindFriendDetails(friend)
            } else {
                // Handle case where the friend is not found (optional)
                finish() // Close the activity if the friend is not found
            }
        }
    }

    private fun bindFriendDetails(friend: Friend) {
        // Set the name and school
        binding.etName.setText(friend.name)
        binding.etSchool.setText(friend.school)
        binding.etBio.setText(friend.bio)

        // Load the photo if available
        if (friend.photo.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(friend.photo)
            if (bitmap != null) {
                binding.ivPhoto.setImageBitmap(bitmap)
            } else {
                binding.ivPhoto.setImageResource(R.drawable.profile) // Default profile image
            }
        } else {
            binding.ivPhoto.setImageResource(R.drawable.profile) // Default profile image
        }
    }
}
