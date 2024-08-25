package com.adista.finalproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.adista.finalproject.database.Friend
import com.adista.finalproject.database.FriendDatabase
import com.adista.finalproject.databinding.ActivityAddFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFriendBinding
    private val imgCAPTURE = 1
    private val reqImgPICK = 2
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAdd.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnSave.setOnClickListener {
            showSaveConfirmationDialog()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        dispatchTakePictureIntent()
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), imgCAPTURE)
                    }
                }
                1 -> choosePhotoFromGallery()
            }
        }
        builder.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                try {
                    val photoFile: File = createImageFile()
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.adista.finalproject.fileprovider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    Log.d("AddFriendActivity", "Photo URI: $photoURI") // Log URI yang digunakan
                    startActivityForResult(takePictureIntent, imgCAPTURE)
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error occurred while creating the File", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        currentPhotoPath = imageFile.absolutePath
        Log.d("AddFriendActivity", "Image file path: $currentPhotoPath")
        return imageFile
    }


    private fun choosePhotoFromGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, reqImgPICK)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API...")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                imgCAPTURE -> {
                    currentPhotoPath?.let {
                        val file = File(it)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(it)
                            binding.ivPhoto.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                reqImgPICK -> {
                    val uri = data?.data
                    if (uri != null) {
                        loadImageFromUri(uri)
                    } else {
                        Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }




    private fun loadImageFromUri(uri: Uri) {
        if (isFileExists(uri)) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivPhoto.setImageBitmap(bitmap)
                inputStream?.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
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
        val photoUri = selectedImageUri?.toString() ?: currentPhotoPath

        if (name.isBlank() || school.isBlank() || photoUri.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val friend = Friend(name = name, school = school, bio = "", photo = photoUri)

        lifecycleScope.launch {
            try {
                val db = FriendDatabase.getDatabase(applicationContext)
                db.friendDao().insertFriend(friend)
                Toast.makeText(this@AddFriendActivity, "Friend's information saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddFriendActivity, "Failed to save friend data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isFileExists(uri: Uri): Boolean {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.close()
            true
        } catch (e: FileNotFoundException) {
            false
        }
    }

}
