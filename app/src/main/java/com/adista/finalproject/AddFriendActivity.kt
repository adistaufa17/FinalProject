package com.adista.finalproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendDatabase
import com.adista.finalproject.databinding.ActivityAddFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class AddFriendActivity : AppCompatActivity() {

    // Variabel untuk view binding agar bisa mengakses elemen pada layout
    private lateinit var binding: ActivityAddFriendBinding
    // Untuk menyimpan URI gambar yang dipilih
    private var selectedImageUri: String? = null
    // Kode request untuk memilih gambar atau mengambil foto
    private val reqImgPICK = 1
    private val reqImgCAPTURE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        // Inisialisasi view binding
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur insets untuk memperhitungkan system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mengatur listener pada tombol-tombol
        binding.btnAdd.setOnClickListener {
            showImagePickerDialog() // Membuka dialog untuk memilih gambar atau mengambil foto
        }

        binding.btnSave.setOnClickListener {
            showSaveConfirmationDialog() // Menampilkan dialog konfirmasi sebelum menyimpan data
        }

        binding.btnBack.setOnClickListener {
            finish() // Menutup aktivitas dan kembali ke layar sebelumnya
        }
    }

    // Menampilkan dialog untuk memilih opsi pengambilan gambar
    private fun showImagePickerDialog() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih opsi")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera() // Opsi untuk mengambil foto
                1 -> choosePhotoFromGallery() // Opsi untuk memilih gambar dari galeri
            }
        }
        builder.show() // Menampilkan dialog
    }

    // Membuka intent untuk mengambil foto menggunakan kamera
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, reqImgCAPTURE) // Memulai aktivitas kamera
    }

    // Membuka intent untuk memilih gambar dari galeri
    private fun choosePhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image" // Mengatur tipe intent menjadi gambar
        startActivityForResult(intent, reqImgPICK) // Memulai aktivitas galeri
    }

    // Metode yang didepresiasi untuk menangani hasil dari pemilihan atau pengambilan gambar
    @Deprecated("Metode ini sudah didepresiasi...")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                reqImgPICK -> {
                    // Menangani gambar yang dipilih dari galeri
                    val uri = data?.data
                    if (uri != null) {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        binding.ivPhoto.setImageBitmap(bitmap) // Menampilkan gambar pada ImageView
                        selectedImageUri = saveImageToInternalStorage(bitmap) // Menyimpan gambar ke penyimpanan internal
                    } else {
                        Toast.makeText(this, "URI gambar tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                reqImgCAPTURE -> {
                    // Menangani gambar yang diambil dari kamera
                    val bitmap = data?.extras?.get("data") as Bitmap
                    binding.ivPhoto.setImageBitmap(bitmap) // Menampilkan gambar pada ImageView
                    selectedImageUri = saveImageToInternalStorage(bitmap) // Menyimpan gambar ke penyimpanan internal
                }
            }
        }
    }

    // Menampilkan dialog konfirmasi sebelum menyimpan data teman
    private fun showSaveConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Simpan Teman")
        builder.setMessage("Apakah Anda yakin ingin menyimpan informasi teman ini?")

        // Jika pengguna mengkonfirmasi, simpan data teman
        builder.setPositiveButton("Ya") { _, _ ->
            saveFriendDataToDatabase()
        }

        builder.setNegativeButton("Tidak", null) // Tidak melakukan apa-apa jika pengguna membatalkan
        builder.show() // Menampilkan dialog
    }

    // Menyimpan data teman ke dalam database
    private fun saveFriendDataToDatabase() {
        // Mengambil data dari input pengguna
        val name = binding.etName.text.toString()
        val school = binding.etSchool.text.toString()
        val bio = binding.etBio.text.toString()
        val bitmap = (binding.ivPhoto.drawable as BitmapDrawable).bitmap
        val photoPath = saveImageToInternalStorage(bitmap)

        // Memvalidasi bahwa semua field telah diisi
        if (name.isBlank() || school.isBlank() || photoPath.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        // Membuat objek Friend untuk disimpan ke dalam database
        val friend = Friend(name = name, school = school, bio = bio, photo = photoPath)

        // Memasukkan data teman ke dalam database menggunakan coroutine
        lifecycleScope.launch {
            try {
                val db = FriendDatabase.getDatabase(applicationContext)
                db.friendDao().insertFriend(friend) // Memasukkan data ke database
                Toast.makeText(this@AddFriendActivity, "Informasi teman berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish() // Menutup aktivitas setelah menyimpan data
            } catch (e: Exception) {
                Toast.makeText(this@AddFriendActivity, "Gagal menyimpan data teman", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Menyimpan gambar bitmap ke penyimpanan internal dan mengembalikan path file
    @SuppressLint("SimpleDateFormat")
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) // Membuat timestamp
        val fileName = "JPEG_$timeStamp.jpg" // Membuat nama file unik untuk gambar
        val file = File(getDir("images", Context.MODE_PRIVATE), fileName) // Membuat file di penyimpanan internal
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos) // Mengompres bitmap dan menyimpannya ke file
            fos.close()
            Toast.makeText(this, "Gambar berhasil disimpan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
        }
        return file.absolutePath // Mengembalikan path file gambar yang disimpan
    }
}
