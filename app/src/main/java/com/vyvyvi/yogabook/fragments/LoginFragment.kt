package com.vyvyvi.yogabook.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vyvyvi.yogabook.LandingActivity
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.utils.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        sharedPreferences =
            requireContext().getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btn = view.findViewById<TextView>(R.id.signupLink)
        btn.setOnClickListener {
            var fr = getFragmentManager()?.beginTransaction()
            fr?.replace(R.id.login_signup_fragment, SignupFragment())
            fr?.commit()
        }

        val usernameEt = view.findViewById<EditText>(R.id.etName)
        val passwordEt = view.findViewById<EditText>(R.id.etPassword)
        val submitBtn = view.findViewById<Button>(R.id.btnSave)

        submitBtn.setOnClickListener {
            val username = usernameEt.text.toString()
            val password = passwordEt.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(
                    context,
                    "Username and Password are required for logging in.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val database = AppDatabase.getDatabase(requireContext())
            val dao = database.appDao()

            CoroutineScope(Dispatchers.IO).launch {
                val loggedIn = dao.login(username, password)

                if (loggedIn != null && loggedIn) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Successfully logged in as $username",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        sharedPreferences.edit().apply {
                            putBoolean(Configuration.firstTimeKey, false)
                            putBoolean(Configuration.loggedInKey, true)
                            putString(Configuration.userIdKey, username)
                        }.apply()
                        val intent = Intent(context, LandingActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to Login. User and password not matching for $username.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }
}