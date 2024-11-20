package com.vyvyvi.yogabook

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.vyvyvi.yogabook.adapters.SessionAdapter
import com.vyvyvi.yogabook.data.AppDao
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Session
import com.vyvyvi.yogabook.data.SessionItem
import com.vyvyvi.yogabook.utils.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.time.LocalDate
import java.util.Calendar


class ProgressActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var sessionDateTv: TextView
    private lateinit var sessionAddressTv: TextView
    private lateinit var locationImg: ImageView
    private lateinit var sessionList: ListView

    private lateinit var currentStreakTv: TextView
    private lateinit var maxStreakTv: TextView

    private lateinit var db: AppDao
    private var username: String? = null
    private lateinit var sessions: List<Session>
    private lateinit var sessionItems: List<SessionItem>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        db = AppDatabase.getDatabase(applicationContext).appDao()
        calendarView = findViewById(R.id.cv)
        sessionDateTv = findViewById(R.id.sessionDate)
        sessionAddressTv = findViewById(R.id.location)
        locationImg = findViewById(R.id.location_icon)
        currentStreakTv = findViewById(R.id.currentStreakValue)
        maxStreakTv = findViewById(R.id.maxStreakValue)

        CoroutineScope(Dispatchers.IO).launch {
            loadData()
            withContext(Dispatchers.Main) {
                calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
                    override fun onClick(calendarDay: CalendarDay) {
                        val clickedDayCalendar = calendarDay.calendar

                        CoroutineScope(Dispatchers.IO).launch {
                            loadSessionAndItems(clickedDayCalendar)
                        }
                    }
                })
            }

            val calendar = Calendar.getInstance()
            loadSessionAndItems(calendar)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadSessionAndItems(calendar: Calendar) {
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH] + 1
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]

        val localDate = LocalDate.of(year, month, dayOfMonth)
        val session = (sessions.filter { it.date == localDate && it.completed }).getOrNull(0)
        sessionList = findViewById(R.id.lv)

        if (session != null) {
            sessionItems = db.getSessionItems(session.session_id)
        } else {
            sessionItems = listOf()
        }

        withContext(Dispatchers.Main) {
            if (session == null) {
                sessionDateTv.text = "You didn't do any yoga on this day :("
                sessionAddressTv.text = ""
                locationImg.visibility = View.GONE
            } else {
                sessionDateTv.text = session.date.toString() + " yoga session"
                if (session.location == null) {
                    sessionAddressTv.text = session.location
                    locationImg.visibility = View.INVISIBLE
                } else {
                    sessionAddressTv.text = session.location
                    locationImg.visibility = View.VISIBLE
                }
            }


            val adapter = SessionAdapter(applicationContext, R.layout.routine_recycler_item, sessionItems)
            sessionList.adapter = adapter


            sessionList.setOnItemClickListener { parent, view, position, id ->
                val builder = AlertDialog.Builder(this@ProgressActivity)
                    .setTitle(sessionItems[position].hindiName)
                    .setMessage("${sessionItems[position].name}: \n${sessionItems[position].description}")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                builder.show()
            }

        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadData() {
        val sharedPreferences =
            getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Configuration.userIdKey, null)

        if (username == null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        sessions = db.getAllSessions(username!!)
        val streak = db.getUserStreak(username!!)

        currentStreakTv.text = streak.current_streak.toString()
        maxStreakTv.text = streak.max_streak.toString()

        val calendar: List<Calendar> = sessions.filter { it.completed }
            .map {
                val calendar: Calendar = Calendar.getInstance()
                it.date?.let {
                    calendar.set(Calendar.YEAR, it.year)
                    calendar.set(Calendar.MONTH, it.monthValue - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, it.dayOfMonth)
                }
                calendar
            }.toList()
        calendarView.setHighlightedDays(calendar)
    }
}