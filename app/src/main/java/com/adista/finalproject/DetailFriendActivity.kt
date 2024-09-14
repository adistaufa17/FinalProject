package com.adista.finalproject

// Import library yang dibutuhkan untuk Intent, Bitmap, Toast, ViewModel, dan sebagainya
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

// Kelas DetailFriendActivity adalah activity yang digunakan untuk menampilkan detail teman
class DetailFriendActivity : AppCompatActivity() {

    // Variabel untuk binding layout dan menyimpan ID teman
    private lateinit var binding: ActivityDetailFriendBinding
    private var friendId: Int = -1

    // Inisialisasi FriendViewModel menggunakan delegasi viewModels untuk mendapatkan data
    private val friendViewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan ViewBinding untuk menghubungkan tampilan dari XML ke activity ini
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengambil friendId yang dikirim melalui Intent dari activity sebelumnya
        friendId = intent.getIntExtra("FRIEND_ID", -1)

        // Jika friendId tidak valid (=-1), tutup activity
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
        // Mengambil data teman berdasarkan ID dari ViewModel dan mengamati perubahannya
        friendViewModel.getFriendById(friendId).observe(this) { friend ->
            if (friend != null) {
                // Jika teman ditemukan, tampilkan detailnya
                bindFriendDetails(friend)
            } else {
                // Jika tidak ditemukan, tutup activity
                finish()
            }
        }
    }

    // Fungsi untuk menampilkan detail teman pada tampilan (TextView dan ImageView)
    private fun bindFriendDetails(friend: Friend) {
        // Mengubah string menjadi Editable untuk ditampilkan di TextView
        binding.tvName.text = friend.name.toEditable()
        binding.tvSchool.text = friend.school.toEditable()
        binding.tvBio.text = friend.bio.toEditable()

        // Jika teman memiliki foto, decode file foto dan tampilkan pada ImageView
        if (friend.photo.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(friend.photo)
            if (bitmap != null) {
                binding.ivPhoto.setImageBitmap(bitmap)
            } else {
                // Jika bitmap tidak valid, gunakan gambar default
                binding.ivPhoto.setImageResource(R.drawable.profile)
            }
        } else {
            // Jika tidak ada foto, gunakan gambar default
            binding.ivPhoto.setImageResource(R.drawable.profile)
        }
    }

    // Fungsi untuk menampilkan dialog konfirmasi penghapusan teman
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Friend") // Judul dialog
        builder.setMessage("Are you sure you want to remove this friend?") // Pesan konfirmasi
        builder.setPositiveButton("Remove") { _, _ ->
            // Jika pengguna memilih "Remove", hapus teman
            deleteFriend()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Jika pengguna memilih "Cancel", tutup dialog
            dialog.dismiss()
        }
        builder.create().show() // Tampilkan dialog
    }

    // Fungsi untuk menghapus teman dari database
    private fun deleteFriend() {
        // Ambil data teman dari ViewModel
        friendViewModel.getFriendById(friendId).observe(this@DetailFriendActivity) { friend ->
            friend?.let {
                // Jika teman ditemukan, hapus teman dari database
                friendViewModel.deleteFriend(it)

                // Kembali ke MainActivity setelah penghapusan
                val destination = Intent(this@DetailFriendActivity, MainActivity::class.java)
                startActivity(destination)
                finish()
            } ?: run {
                // Jika tidak ada teman dengan ID tersebut, tampilkan pesan error
                Toast.makeText(this@DetailFriendActivity, "No friend found with ID: $friendId", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi ekstensi untuk mengubah String menjadi Editable agar bisa ditampilkan di TextView
    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}