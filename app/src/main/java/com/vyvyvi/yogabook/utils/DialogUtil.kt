package com.vyvyvi.yogabook.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.AppDatabase
import kotlinx.coroutines.Job

object DialogUtil {
    fun showNumberAdjustmentDialog(context: Context, initialValue: Int, callback: (Int) -> Job) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.duration_dialog, null)

        val numberTextView = dialogView.findViewById<EditText>(R.id.number_text_view)
        val incrementButton = dialogView.findViewById<Button>(R.id.increment_button)
        val decrementButton = dialogView.findViewById<Button>(R.id.decrement_button)

        numberTextView.setText(initialValue.toString())

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                callback(numberTextView.text.toString().toInt())
            }
            .setNegativeButton("Cancel", null)
            .create()

        incrementButton.setOnClickListener {
            val currentValue = numberTextView.text.toString().toInt()
            numberTextView.setText("${currentValue + 15}")
        }

        decrementButton.setOnClickListener {
            val currentValue = numberTextView.text.toString().toInt()
            if (currentValue > 0) {
                numberTextView.setText("${currentValue - 15}")
            }
        }

        dialog.show()
    }
}