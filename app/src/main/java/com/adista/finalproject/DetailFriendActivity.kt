package com.adista.finalproject

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

    private val friendViewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val friendId = intent.getIntExtra("FRIEND_ID", -1)
        if (friendId == -1) {
            finish() // Close activity if ID is not valid
            return
        }

        loadFriendDetails(friendId)

        binding.btnEdit.setOnClickListener {
            val destination = Intent(this, EditFriendActivity::class.java).apply {
                putExtra("FRIEND_ID", friendId) // Pass the friendId to the EditFriendActivity
            }
            startActivity(destination)
        }

        binding.btnDelete.setOnClickListener {
            friendViewModel.getFriendById(friendId).observe(this) { friend ->
                if (friend != null) {
                    friendViewModel.deleteFriend(friend)
                    finish()
                }
            }
        }

    }

    private fun loadFriendDetails(friendId: Int) {
        friendViewModel.getFriendById(friendId).observe(this) { friend ->
            if (friend != null) {
                bindFriendDetails(friend)
            } else {
                finish()
            }
        }
    }

    private fun bindFriendDetails(friend: Friend) {
        binding.etName.setText(friend.name)
        binding.etSchool.setText(friend.school)
        binding.etBio.setText(friend.bio)

        if (friend.photo.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(friend.photo)
            if (bitmap != null) {
                binding.ivPhoto.setImageBitmap(bitmap)
            } else {
                binding.ivPhoto.setImageResource(R.drawable.profile)
            }
        } else {
            binding.ivPhoto.setImageResource(R.drawable.profile)
        }
    }
}
