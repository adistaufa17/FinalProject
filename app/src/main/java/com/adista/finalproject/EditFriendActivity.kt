package com.adista.finalproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityEditFriendBinding
import kotlinx.coroutines.launch

class EditFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFriendBinding
    private var friendId: Int = -1
    private val friendViewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        friendId = intent.getIntExtra("FRIEND_ID", -1)
        if (friendId == -1) {
            finish() // Close activity if ID is not valid
            return
        }

        loadFriendDetails(friendId)

        binding.btnBack.setOnClickListener {
            val destination = Intent(this, DetailFriendActivity::class.java)
            startActivity(destination)
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

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Friend")
        builder.setMessage("Are you sure you want to remove this friend?")
        builder.setPositiveButton("Remove") { _, _ ->
            // Jika dihapus, panggil fungsi deleteFriend()
            deleteFriend()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteFriend() {
        lifecycleScope.launch {
            friendViewModel.getFriendById(friendId).observe(this@EditFriendActivity) { friend ->
                friend?.let {
                    // Menghapus teman dan kembali ke MainActivity
                    friendViewModel.deleteFriend(it)
                    val destination = Intent(this@EditFriendActivity, MainActivity::class.java)
                    startActivity(destination)
                    finish()
                } ?: run {
                    Toast.makeText(this@EditFriendActivity, "No friend found with ID: $friendId", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
