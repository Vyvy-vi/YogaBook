package com.vyvyvi.yogabook

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // Create an Intent to launch the MainActivity
        val intentMain = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        var type = intent.getStringExtra("type")
        if (type == null) {
            type = "alarm"
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            AlertsActivity.ALARM_REQUEST_CODE,
            intentMain,
            PendingIntent.FLAG_IMMUTABLE)

        val title = if (type == "water") "Water Reminder" else "Daily Reminder"
        val text = if (type == "water") "Remember to drink your water!" else "Remember to do your yoga today"

        val builder = NotificationCompat.Builder(context, "yogabook_notification")
            .setSmallIcon(R.mipmap.yogabook_logo_round)
            .setLargeIcon(context.getDrawable(R.drawable.applogo_graphic)?.toBitmap())
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("yogabook_notification", "yoga_reminder", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        if (type == "water") {
            var mp = MediaPlayer.create(context, R.raw.minecraft_alarm)
            Log.d("Daily Reminder", "Remember to drink your water")
            mp.start()
            Toast.makeText(context, "Drink Water!", Toast.LENGTH_LONG).show()
        } else {
            var mp = MediaPlayer.create(context, R.raw.berserk_behelit_alarm)
            Log.d("Daily Reminder", "Remember to do your yoga today")
            mp.start()
            Toast.makeText(context, "Start your yoga!", Toast.LENGTH_LONG).show()
        }
        notificationManager.notify(0, builder.build())
    }
}

