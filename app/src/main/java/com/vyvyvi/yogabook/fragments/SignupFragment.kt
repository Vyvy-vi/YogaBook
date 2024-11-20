package com.vyvyvi.yogabook.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.vyvyvi.yogabook.LandingActivity
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.User
import com.vyvyvi.yogabook.utils.Configuration
import com.vyvyvi.yogabook.utils.DataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var getImage: ActivityResultLauncher<String>
    var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btn = view.findViewById<TextView>(R.id.loginLink)
        btn.setOnClickListener {
            var fr = getFragmentManager()?.beginTransaction()
            fr?.replace(R.id.login_signup_fragment, LoginFragment())
            fr?.commit()
        }


        val usernameEt = view.findViewById<EditText>(R.id.etName)
        val emailEt = view.findViewById<EditText>(R.id.etEmail)
        val passwordEt = view.findViewById<EditText>(R.id.etPassword)

        val submitBtn = view.findViewById<Button>(R.id.btnSave)

        val avatar = view.findViewById<ImageView>(R.id.avatar)

        sharedPreferences =
            requireContext().getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != null) {
                    imageUri = it
                }
                avatar.setImageURI(imageUri)
            })

        avatar.setOnClickListener {
            getImage.launch("image/*")
        }

        submitBtn.setOnClickListener {
            val username = usernameEt.text.toString()
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()

            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(
                    context,
                    "Username, Email and Password are required for signing up.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val database = AppDatabase.getDatabase(requireContext())
            val dao = database.appDao()

            CoroutineScope(Dispatchers.IO).launch {
                val userExists = dao.userExists(username)
                if (userExists != null && userExists) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to Signup. User with username $username already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    var bitmap = avatar.drawable.toBitmap()
                    val user = User(username, email, password, DataUtils.bitmapToByteArray(bitmap))
                    dao.insertUser(user)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Successfully signed up as $username",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        val firstTime = sharedPreferences.getBoolean(Configuration.firstTimeKey, true)
                        Log.d("TEXT", firstTime.toString())
                        if (firstTime) {
                            DataUtils.seedPoseData(requireContext(), username)
                        }
                        DataUtils.seedUserStreak(requireContext(), username)

                        sharedPreferences.edit().apply {
                            putBoolean(Configuration.firstTimeKey, false)
                            putBoolean(Configuration.loggedInKey, true)
                            putString(Configuration.userIdKey, username)
                        }.apply()

                        val intent = Intent(context, LandingActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}