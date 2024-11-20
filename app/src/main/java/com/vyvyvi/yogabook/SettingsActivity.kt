package com.vyvyvi.yogabook

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vyvyvi.yogabook.AlertsActivity.Companion.permissions
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.User
import com.vyvyvi.yogabook.utils.Configuration
import com.vyvyvi.yogabook.utils.DataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var avatarImg: ImageView
    private lateinit var trackLocationCb: CheckBox

    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var logoutBtn: Button

    private lateinit var editFab: FloatingActionButton

    private var username: String? = null
    private var imageUri: Uri? = null
    private lateinit var getImage: ActivityResultLauncher<String>

    private val LOCATION_PERMISSION_REQ_CODE = 1001
    private val permission = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences =
            getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)

        val username = sharedPreferences.getString(Configuration.userIdKey, null)

        checkLoggedIn()

        emailEt = findViewById(R.id.etEmail)
        passwordEt = findViewById(R.id.etPassword)
        trackLocationCb = findViewById(R.id.trackLocation)
        avatarImg = findViewById(R.id.avatarView)
        saveBtn = findViewById(R.id.btnSave)
        deleteBtn = findViewById(R.id.btnDelete)
        logoutBtn = findViewById(R.id.btnLogout)
        editFab = findViewById(R.id.editFab)

        username?.let {
            loadData(username)
        }

        saveBtn.setOnClickListener {
            username?.let {
                saveData(username)
            }
        }

        deleteBtn.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "Deleting all user data from $username...",
                Toast.LENGTH_SHORT
            ).show()
            val db = AppDatabase.getDatabase(applicationContext).appDao()
            CoroutineScope(Dispatchers.IO).launch {
                username?.let { db.deleteUser(username) }
            }
            logout()
        }

        logoutBtn.setOnClickListener {
            logout()
        }

        getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != null) {
                    imageUri = it
                }
                avatarImg.setImageURI(imageUri)
            })

        editFab.setOnClickListener {
            showImagePickerDialog()
        }

        trackLocationCb.setOnClickListener {
            if (permissions.toList().any {
                    ActivityCompat.checkSelfPermission(this@SettingsActivity, it) !=
                            PackageManager.PERMISSION_GRANTED
                }) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQ_CODE
                )
            }
        }
    }

    private fun showImagePickerDialog() {
        val dialog = Dialog(this)
        dialog.setTitle("Choose image")
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.imagepick_dialog)
        dialog.show()

        val imageView = dialog.findViewById(R.id.image_view) as ImageView
        val btnGallery = dialog.findViewById(R.id.btn_gallery) as Button
        val btnUrl = dialog.findViewById(R.id.btn_url) as Button
        val btnFetch = dialog.findViewById(R.id.btn_fetch) as Button
        val urlEt = dialog.findViewById(R.id.urlEt) as EditText
        val btnCancel = dialog.findViewById(R.id.btn_cancel) as ImageView

        imageView.setImageDrawable(avatarImg.drawable)

        btnGallery.setOnClickListener {
            dialog.dismiss()
            getImage.launch("image/*")
        }

        btnUrl.setOnClickListener {
            btnGallery.visibility = View.GONE
            urlEt.visibility = View.VISIBLE
            btnFetch.visibility = View.VISIBLE
            btnUrl.visibility = View.GONE
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnFetch.setOnClickListener {
            val url = urlEt.text.toString()
            dialog.dismiss()
            Toast.makeText(applicationContext, "downloading file from url...", Toast.LENGTH_SHORT)
                .show()
            downloadFile(url)
        }
    }

    private fun downloadFile(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "failed to download file from $url...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Successfully downloaded file...",
                        Toast.LENGTH_SHORT
                    ).show()
                    val inputStream = response.body?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    avatarImg.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun logout() {
        Toast.makeText(applicationContext, "Logging you out...", Toast.LENGTH_SHORT).show()
        sharedPreferences.edit().apply {
            putBoolean(Configuration.loggedInKey, false)
            putString(Configuration.userIdKey, null)
        }.apply()

        val i = Intent(applicationContext, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun checkLoggedIn() {
        sharedPreferences =
            getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Configuration.userIdKey, null)
        if (username == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("component", "login")
            startActivity(intent)
        }
        val nameTv = findViewById<TextView>(R.id.username)
        nameTv.text = username
    }

    private fun loadData(username: String): User? {
        val db = AppDatabase.getDatabase(applicationContext).appDao()
        var user: User? = null
        CoroutineScope(Dispatchers.IO).launch {
            user = db.getUser(username)
            withContext(Dispatchers.Main) {
                emailEt.setText(user?.email)
                passwordEt.setText(user?.password)
                val bitmap = user?.avatar?.let { DataUtils.byteArrayToBitmap(it) }
                avatarImg.setImageBitmap(bitmap)
                Log.d("Text", bitmap.toString())
                if (user?.trackLocation == true)
                    trackLocationCb.isChecked = true
            }
        }
        return user
    }

    private fun saveData(username: String) {
        val db = AppDatabase.getDatabase(applicationContext).appDao()
        val email = emailEt.text.toString()
        val password = passwordEt.text.toString()
        val isChecked = trackLocationCb.isChecked
        val avatar = DataUtils.bitmapToByteArray(avatarImg.drawable.toBitmap())
        Log.d("Text", avatarImg.drawable.toBitmap().toString())
        val user = User(
            username = username,
            email = email,
            password = password,
            trackLocation = isChecked,
            avatar = avatar
        )
        CoroutineScope(Dispatchers.IO).launch {
            db.updateUser(user)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    "Successfully updated user details...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}