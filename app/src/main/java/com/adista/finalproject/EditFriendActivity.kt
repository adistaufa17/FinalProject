package com.adista.finalproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendDatabase
import com.adista.finalproject.database.FriendViewModel
import com.adista.finalproject.databinding.ActivityEditFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date


@Suppress("DEPRECATION")
class EditFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFriendBinding
    private var friendId: Int = -1
    private val friendViewModel: FriendViewModel by viewModels()
    private val reqImgCap = 1
    private val reqImgGal = 2


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
            finish()
        }

        binding.btnSave.setOnClickListener {
            showSaveConfirmationDialog()
        }

        binding.btnCamera.setOnClickListener {
            showImageSourceSelectionDialog()
        }

    }

    private fun showImageSourceSelectionDialog() {
        val items = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> dispatchGalleryIntent()
                }
            }
            .show()
    }

    private fun dispatchGalleryIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { galleryIntent ->
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, reqImgGal)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, reqImgCap)
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                reqImgCap -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.ivPhoto.setImageBitmap(imageBitmap)
                    saveImageToInternalStorage(imageBitmap)
                }
                reqImgGal -> {
                    val selectedImageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    binding.ivPhoto.setImageBitmap(imageBitmap)
                    saveImageToInternalStorage(imageBitmap)
                }
            }
        }
    }


    private fun showSaveConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Friend")
        builder.setMessage("Are you sure you want to save this friend's information?")

        builder.setPositiveButton("Yes") { _, _ ->
            saveFriendDataToDatabase()
        }

        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun saveFriendDataToDatabase() {
        val name = binding.etName.text.toString()
        val school = binding.etSchool.text.toString()
        val bio = binding.etBio.text.toString()
        val bitmap = (binding.ivPhoto.drawable as BitmapDrawable).bitmap
        val photoPath = saveImageToInternalStorage(bitmap)

        if (name.isBlank() || school.isBlank() || photoPath.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedFriend = Friend(friendId, name, school, bio, photoPath) // Membuat objek teman yang diperbarui

        lifecycleScope.launch {
            try {
                val db = FriendDatabase.getDatabase(applicationContext)
                db.friendDao().updateFriend(updatedFriend) // Melakukan operasi update pada teman yang ada
                Toast.makeText(this@EditFriendActivity, "Friend's information updated successfully", Toast.LENGTH_SHORT).show()

                // Navigasi langsung ke MainActivity setelah berhasil menyimpan data
                val destination = Intent(this@EditFriendActivity, MainActivity::class.java)
                startActivity(destination)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditFriendActivity, "Failed to update friend data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "JPEG_$timeStamp.jpg"
        val file = File(getDir("images", Context.MODE_PRIVATE), fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
        return file.absolutePath
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
