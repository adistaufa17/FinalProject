package com.adista.finalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendDatabase
import com.adista.finalproject.databinding.ActivityDetailFriendBinding
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class DetailFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFriendBinding
    private lateinit var friend: Friend
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        friend = intent.getParcelableExtra("friend")!!

        binding.etName.setText(friend.name)
        binding.etSchool.setText(friend.school)
        binding.etBio.setText(friend.bio)

        val uri = Uri.parse(friend.photo)
        binding.ivPhoto.setImageURI(uri)

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditFriendActivity    ::class.java)
            intent.putExtra("friend", friend)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Friend")
        builder.setMessage("Are you sure you want to delete this friend's information?")

        builder.setPositiveButton("Yes") { _, _ ->
            deleteFriendDataFromDatabase()
        }

        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun deleteFriendDataFromDatabase() {
        lifecycleScope.launch {
            try {
                val db = FriendDatabase.getDatabase(applicationContext)
                db.friendDao().deleteFriend(friend)
                Toast.makeText(this@DetailFriendActivity, "Friend's information deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@DetailFriendActivity , "Failed to delete friend data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
