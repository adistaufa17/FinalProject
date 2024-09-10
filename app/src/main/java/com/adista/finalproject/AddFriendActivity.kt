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

    private lateinit var binding: ActivityAddFriendBinding
    private var selectedImageUri: String? = null
    private val reqImgPICK = 1
    private val reqImgCAPTURE = 2

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
                0 -> openCamera()
                1 -> choosePhotoFromGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, reqImgCAPTURE)
    }

    private fun choosePhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, reqImgPICK)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                reqImgPICK -> {
                    val uri = data?.data
                    if (uri != null) {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        binding.ivPhoto.setImageBitmap(bitmap)
                        selectedImageUri = saveImageToInternalStorage(bitmap)
                    } else {
                        Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
                    }
                }
                reqImgCAPTURE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    binding.ivPhoto.setImageBitmap(bitmap)
                    selectedImageUri = saveImageToInternalStorage(bitmap)
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

        val friend = Friend(name = name, school = school, bio = bio, photo = photoPath)

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
}
