package com.vyvyvi.yogabook

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobScheduler
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Calendar

class AlertsActivity : AppCompatActivity() {
    var jobScheduler: JobScheduler? = null

    companion object {
        val ALARM_REQUEST_CODE = 5015
        val permissions = arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.SET_ALARM
        )
        val ALARM_PERMISSION_REQUEST_CODE = 5010
        val WATER_PERMISSION_REQUEST_CODE = 5020
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        val stopReminder = findViewById<Button>(R.id.stop_alarm)
        val startReminder = findViewById<Button>(R.id.start_alarm)

        val startWaterReminder = findViewById<Button>(R.id.start_water_reminder)
        val stopWaterReminder = findViewById<Button>(R.id.stop_water_reminder)

        startReminder.setOnClickListener {
            requestPermissions()
        }

        startWaterReminder.setOnClickListener {
            requestPermissions(true)
        }

        // TODO - make logic for stopping the alarms.
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ALARM_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setAlarm()
            }
        } else if (requestCode == WATER_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setWaterReminder()
            }
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissions(isWater: Boolean = false) {
        if (permissions.toList().any {
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                if (isWater) WATER_PERMISSION_REQUEST_CODE else ALARM_PERMISSION_REQUEST_CODE
            )
        } else {
            if (isWater)
                setWaterReminder()
            else
                setAlarm()
        }
    }

    private fun setWaterReminder() {
        // TODO -> Make new receiver and maybe turn this into a job
        val alarmManager =
            getSystemService(AlarmManager::class.java)
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("type", "water")
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, 10)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            1000,
            pendingIntent
        )
    }

    private fun setAlarm() {
        val alarmManager =
            getSystemService(AlarmManager::class.java)
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("type", "water")
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, 10)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}