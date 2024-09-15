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
            finish()
            return
        }

        // Memuat detail teman berdasarkan friendId
        loadFriendDetails(friendId)

        // Menonaktifkan editing pada TextView (field nama, sekolah, dan bio)
        binding.tvName.isEnabled = false
        binding.tvSchool.isEnabled = false
        binding.tvBio.isEnabled = false

        // Ketika tombol edit ditekan, buka EditFriendActivity dan kirim friendId
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditFriendActivity::class.java)
            intent.putExtra("FRIEND_ID", friendId)
            startActivity(intent)
        }

        // Ketika tombol delete ditekan, tampilkan dialog konfirmasi penghapusan
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Ketika tombol back ditekan, kembali ke MainActivity
        binding.btnBack.setOnClickListener {
            val destination = Intent(this, MainActivity::class.java)
            startActivity(destination)
        }
    }

    // Fungsi untuk memuat detail teman berdasarkan ID
    private fun loadFriendDetails(friendId: Int) {
        friendViewModel.getFriendById(friendId).observe(this) { friend ->
            if (friend != null) {
                bindFriendDetails(friend)
            } else {
                finish()
            }
        }
    }

    // Fungsi untuk menampilkan detail teman pada tampilan (TextView dan ImageView)
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

    // Fungsi untuk menampilkan dialog konfirmasi penghapusan teman
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus Teman")
        builder.setMessage("Apakah Anda yakin ingin menghapus informasi teman ini?") // Pesan konfirmasi
        builder.setPositiveButton("Remove") { _, _ ->
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

                friendViewModel.deleteFriend(it)

                val destination = Intent(this@DetailFriendActivity, MainActivity::class.java)
                startActivity(destination)
                finish()
            } ?: run {

                Toast.makeText(this@DetailFriendActivity, "No friend found with ID: $friendId", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}