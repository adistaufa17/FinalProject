package com.adista.finalproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityDetailFriendBinding

class DetailFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFriendBinding
    private var friendId: Int = -1
    private val friendViewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendId = intent.getIntExtra("FRIEND_ID", -1)
        if (friendId == -1) {
            finish() // Close activity if ID is not valid
            return
        }

        loadFriendDetails(friendId)

        // Disable focus and editing for name, school, and bio fields
        binding.tvName.isEnabled = false
        binding.tvSchool.isEnabled = false
        binding.tvBio.isEnabled = false

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditFriendActivity::class.java)
            intent.putExtra("FRIEND_ID", friendId)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            friendViewModel.getFriendById(friendId).observe(this) { friend ->
                if (friend != null) {
                    friendViewModel.deleteFriend(friend)
                    finish()
                }
            }
            showDeleteConfirmationDialog()
        }

        binding.btnBack.setOnClickListener {
            val destination = Intent(this, MainActivity::class.java)
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
    // Fungsi ekstensi untuk mengubah String menjadi Editable
    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun bindFriendDetails(friend: Friend) {
        binding.tvName.text = friend.name.toEditable()
        binding.tvSchool.text = friend.school.toEditable()
        binding.tvBio.text = friend.bio.toEditable()

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
            // If deleted, call deleteFriend() function
            deleteFriend()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteFriend() {
        friendViewModel.getFriendById(friendId).observe(this@DetailFriendActivity) { friend ->
            friend?.let {
                // Delete the friend and return to MainActivity
                friendViewModel.deleteFriend(it)
                val destination = Intent(this@DetailFriendActivity, MainActivity::class.java)
                startActivity(destination)
                finish()
            } ?: run {
                Toast.makeText(this@DetailFriendActivity, "No friend found with ID: $friendId", Toast.LENGTH_SHORT).show()
            }
        }
    }
}