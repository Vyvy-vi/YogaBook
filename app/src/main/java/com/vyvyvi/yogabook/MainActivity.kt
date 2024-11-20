package com.vyvyvi.yogabook

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.vyvyvi.yogabook.fragments.LoginFragment
import com.vyvyvi.yogabook.fragments.SignupFragment
import com.vyvyvi.yogabook.utils.Configuration

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = intent.getStringExtra("component")

        if (component == null)
            setContentView(R.layout.splash_screen)

        sharedPreferences =
            getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        // sharedPreferences.edit().clear().apply()

        val loggedIn = sharedPreferences.getBoolean(Configuration.loggedInKey, false)
        val firstTime = sharedPreferences.getBoolean(Configuration.firstTimeKey, true)
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val i: Intent

                if (loggedIn && (component == null || component == "landing")) {
                    i = Intent(this, LandingActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    setContentView(R.layout.activity_main)
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    if (!firstTime || component == "login") {
                        fragmentTransaction.replace(R.id.login_signup_fragment, LoginFragment())
                    } else {
                        fragmentTransaction.replace(R.id.login_signup_fragment, SignupFragment())
                    }
                    fragmentTransaction.commit()
                }
            }, (if (component == null) 5000 else 2)
        )
    }
}