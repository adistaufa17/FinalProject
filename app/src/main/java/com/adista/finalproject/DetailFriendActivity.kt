package com.adista.finalproject

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityDetailFriendBinding

class DetailFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFriendBinding
    private lateinit var friendViewModel: FriendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan ID teman dari Intent
        val friendId: Int = intent.getIntExtra("FRIEND_ID", 0)

        // Memastikan friendViewModel sudah diinisialisasi
        friendViewModel = FriendViewModel(application)

        // Menggunakan LiveData untuk mengamati perubahan data Friend berdasarkan ID
        friendViewModel.getFriendById(friendId).observe(this, Observer { friend ->
            // Cek apakah Friend ditemukan
            friend?.let {
                // Mengisi tampilan dengan data Friend
                binding.etName.setText(friend.name)
                binding.etSchool.setText(friend.school)
                binding.etBio.setText(friend.bio)

                // Menampilkan foto jika tersedia
                if (it.photo.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeFile(it.photo)
                    binding.ivPhoto.setImageBitmap(bitmap)
                } else {
                    binding.ivPhoto.setImageResource(R.drawable.profile)
                }
            } ?: run {
                Toast.makeText(this, "Friend not found", Toast.LENGTH_SHORT).show()
            }
        })

        // Tombol hapus teman
        binding.btnDelete.setOnClickListener {
            friendViewModel.getFriendById(friendId).observe(this, Observer { friend ->
                friend?.let {
                    // Menghapus data teman
                    friendViewModel.deleteFriend(it)
                    Toast.makeText(this, "Friend deleted", Toast.LENGTH_SHORT).show()
                    finish() // Menutup activity setelah penghapusan
                } ?: run {
                    Toast.makeText(this, "Friend not found", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}