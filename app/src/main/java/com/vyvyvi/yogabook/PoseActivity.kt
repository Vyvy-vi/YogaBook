package com.vyvyvi.yogabook

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Pose
import com.vyvyvi.yogabook.utils.InternalStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PoseActivity : AppCompatActivity() {

    private lateinit var nameEt: EditText
    private lateinit var hindiNameEt: EditText
    private lateinit var descriptionEt: EditText
    private lateinit var img: ImageView
    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button

    private var imageUri: Uri? = null
    private lateinit var getImage: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose)

        nameEt = findViewById(R.id.etName)
        hindiNameEt = findViewById(R.id.etHindiName)
        descriptionEt = findViewById(R.id.etDescription)
        img = findViewById(R.id.image_view)
        saveBtn = findViewById(R.id.btnSave)
        deleteBtn = findViewById(R.id.btnDelete)

        var pose_id: Long = intent.getLongExtra("id", 0)
        populateFields()

        getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != null) {
                    imageUri = it
                }
                img.setImageURI(imageUri)
            })

        img.setOnClickListener {
            getImage.launch("image/*")
        }

        val db = AppDatabase.getDatabase(applicationContext).appDao()
        saveBtn.setOnClickListener {

            val filename = convertToFileName(nameEt.text.toString())
            val pose = Pose(
                id=pose_id,
                name = nameEt.text.toString(),
                hindiName = hindiNameEt.text.toString(),
                description = descriptionEt.text.toString(),
                imageFilename = filename
            )
            InternalStorageHelper.saveImageToInternalStorage(
                applicationContext,
                filename,
                img.drawable.toBitmap()
            )
            CoroutineScope(Dispatchers.IO).launch {
                if (pose_id == 0L)
                    pose_id = db.insertPose(pose)
                else
                    db.updatePose(pose)
            }
            Toast.makeText(applicationContext, "Updated pose data", Toast.LENGTH_SHORT).show()
        }

        deleteBtn.setOnClickListener {
            if (pose_id == 0L) {
                Toast.makeText(applicationContext, "This pose doesn't exist in db", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                db.deletePose(pose_id)
            }
            Toast.makeText(applicationContext, "Deleted pose data", Toast.LENGTH_SHORT).show()
            val i = Intent(this, CatalogActivity::class.java)
            startActivity(i)
        }
    }

    fun convertToFileName(name: String): String {
        val regex = Regex("[^A-Za-z0-9 ]")
        val timestamp = "_${System.currentTimeMillis()}"
        return name
            .trim()
            .replace(regex, "_")
            .replace(" ", "_")
            .take(20) + timestamp

    }

    private fun populateFields() {
        val name = intent.getStringExtra("name")
        val hindiName = intent.getStringExtra("hindiName")
        val description = intent.getStringExtra("description")
        val imageFilename = intent.getStringExtra("imageFilename")

        if (name != null)
            nameEt.setText(name)
        if (hindiName != null)
            hindiNameEt.setText(hindiName)
        if (description != null)
            descriptionEt.setText(description)

        if (imageFilename != null) {
            val bitmap = InternalStorageHelper.loadImageFromInternalStorage(this, imageFilename)
            img.setImageBitmap(bitmap)
        }
    }
}