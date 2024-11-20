package com.vyvyvi.yogabook

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.GridView
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.vyvyvi.yogabook.adapters.MenuGridAdapter
import com.vyvyvi.yogabook.adapters.items
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.utils.Configuration
import com.vyvyvi.yogabook.utils.DataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandingActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val heroCard = findViewById<CardView>(R.id.heroCard)
        heroCard.setOnClickListener {
            val intent = Intent(this, SessionActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        loadGrid()

        checkLoggedIn()
        updateStreak()
        loadAvatar()
    }

    private fun loadGrid() {
        val grid = findViewById<GridView>(R.id.menuGrid)
        grid.adapter = MenuGridAdapter(applicationContext)
        grid.setOnItemClickListener{ adapterView, view, i, l ->
            val i = Intent(applicationContext, items[i].activity)

            startActivity(i)
        }
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

        val nameEt = findViewById<TextView>(R.id.username)
        nameEt.text = username
    }

    private fun loadAvatar() {
        val db = AppDatabase.getDatabase(applicationContext).appDao()
        val avatar = findViewById<ImageView>(R.id.avatar)
        CoroutineScope(Dispatchers.IO).launch {
            val avatarArr = username?.let { db.getAvatar(it) }
            val bitmap = avatarArr?.let { DataUtils.byteArrayToBitmap(it) }

            withContext(Dispatchers.Main) {
                avatar.setImageBitmap(bitmap)
                avatar.setOnClickListener {
                    val intent = Intent(applicationContext, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun updateStreak() {
        val streakTv = findViewById<TextView>(R.id.streak)
        val streakRating = findViewById<RatingBar>(R.id.streak_rating)

        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.appDao()

        CoroutineScope(Dispatchers.IO).launch {
            var streak = username?.let { dao.getStreak(it) }

            if (streak == null) streak = 0;
            streakTv.text = streakTv.text.toString().replace("0", streak.toString())
            if (streak == 0) {
                streakRating.rating = 0f
            } else if (streak <= 2) {
                streakRating.rating = 1f
            } else if (streak <= 4) {
                streakRating.rating = 2f
            } else if (streak <= 6) {
                streakRating.rating = 3f
            } else if (streak <= 10) {
                streakRating.rating = 4f
            } else {
                streakRating.rating = 5f
            }
        }
    }
}