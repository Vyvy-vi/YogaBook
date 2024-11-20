package com.vyvyvi.yogabook

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vyvyvi.yogabook.adapters.HorizontalListAdapter
import com.vyvyvi.yogabook.customviews.CustomProgressBar
import com.vyvyvi.yogabook.data.AppDao
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Session
import com.vyvyvi.yogabook.data.SessionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Period


class TrackActivity : AppCompatActivity(), HorizontalListAdapter.OnItemClickListener {
    private lateinit var rv: RecyclerView
    private lateinit var cancelBtn: ImageView
    private lateinit var rootView: ViewGroup
    private lateinit var sessionItems: List<SessionItem>
    private lateinit var hindiName: TextView
    private lateinit var englishName: TextView
    private lateinit var helpBtn: ImageView


    private var activeItemIndex = 1
    private lateinit var adapter: HorizontalListAdapter
    private lateinit var db: AppDao

    private var sessionId: Long? = null
    private var username: String? = null

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        sessionId = intent.getLongExtra("session_id", 0L).takeIf { it != 0L }
        username = intent.getStringExtra("username")

        if (sessionId == null || username == null) {
            val i = Intent(this, LandingActivity::class.java)
            startActivity(i)
            finish()
        }

        lifecycleScope.launch {
            sessionId?.let { loadData(it) }
        }

        cancelBtn = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener {
            val i = Intent(this, LandingActivity::class.java)
            startActivity(i)
            finish()
        }

        rootView =
            findViewById<ViewGroup>(R.id.progress)
        englishName = findViewById(R.id.englishName)
        hindiName = findViewById(R.id.hindiName)
        helpBtn = findViewById(R.id.help)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadData(sessionId: Long) {
        db = AppDatabase.getDatabase(applicationContext).appDao()
        sessionItems = db.getSessionItems(sessionId)

        withContext(Dispatchers.Main) {

            adapter = HorizontalListAdapter(this@TrackActivity, sessionItems, this@TrackActivity)

            val layoutManager =
                LinearLayoutManager(this@TrackActivity, LinearLayoutManager.HORIZONTAL, false)
            rv = findViewById<View>(R.id.sessionItemBar) as RecyclerView
            rv.setLayoutManager(layoutManager)
            rv.adapter = adapter

            setActive(0)
        }
    }

    override fun onItemClick(position: Int) {
        setActive(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun finishSession() {
        CoroutineScope(Dispatchers.IO).launch {
            sessionId?.let { db.completeSession(it) }
            var streak = username?.let { db.getUserStreak(it) }


            if (streak != null) {


                if (streak.last_streak_date == null) {
                    streak.current_streak = 1
                } else {
                    val gap = Period
                        .between(streak.last_streak_date, LocalDate.now()).days
                    if (gap == 1) {
                        streak.current_streak++
                    } else {
                        streak.current_streak = 1
                    }
                }

                if (streak.current_streak > streak.max_streak)
                    streak.max_streak = streak.current_streak
                streak.last_streak_date = LocalDate.now()

                db.updateUserStreak(streak)

                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext,"Your just hit a streak of ${streak.current_streak}", Toast.LENGTH_LONG).show()
                }
            }
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("You've wrapped up another awesome Yoga session!!!\nYou've taken a step towards a healthier, happier you.\nWe're proud of you :)")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val i = Intent(this, LandingActivity::class.java)
                startActivity(i)
                finish()
            }
            .create()
        builder.show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setActive(position: Int) {
        job?.cancel()

        hindiName.text = sessionItems[position].hindiName
        englishName.text = sessionItems[position].name

        if (position == 0) {
            rv.smoothScrollToPosition(0)
        } else if (activeItemIndex < position) {
            rv.smoothScrollToPosition(position + 1)
        } else if (activeItemIndex >= position) {
            rv.smoothScrollToPosition(position - 1)
        }

        activeItemIndex = position
        adapter.setActiveItem(activeItemIndex)

        val customView = CustomProgressBar(
            applicationContext,
            bitmapFile = sessionItems[activeItemIndex].imgFilename,
            duration = sessionItems[activeItemIndex].duration * 1000L
        )
        helpBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle(sessionItems[activeItemIndex].hindiName)
                .setMessage(sessionItems[activeItemIndex].description)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            builder.show()

        }

        job = CoroutineScope(Dispatchers.Main).launch {
            rootView.removeAllViews()
            rootView.addView(customView)
            delay(sessionItems[activeItemIndex].duration * 1000 + 10L)

            if (activeItemIndex == sessionItems.size - 1) {
                finishSession()
            } else
                setActive(position + 1)
        }


    }
}