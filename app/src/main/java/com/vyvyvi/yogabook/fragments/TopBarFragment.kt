package com.vyvyvi.yogabook.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.vyvyvi.yogabook.AlertsActivity
import com.vyvyvi.yogabook.CatalogActivity
import com.vyvyvi.yogabook.PoseActivity
import com.vyvyvi.yogabook.ProgressActivity
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.RoutineActivity
import com.vyvyvi.yogabook.SessionActivity
import com.vyvyvi.yogabook.SettingsActivity
import com.vyvyvi.yogabook.adapters.SessionAdapter
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Routine
import com.vyvyvi.yogabook.utils.Configuration
import com.vyvyvi.yogabook.utils.DataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopBarFragment : Fragment() {
    private lateinit var textView: TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var settingBtn: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_bar, container, false)
        textView = view.findViewById(R.id.bar_title)
        settingBtn = view.findViewById(R.id.avatar)


        sharedPreferences =
            requireContext().getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(Configuration.userIdKey, null)

        if (username != null)
            loadAvatar(username)

        val activity = requireActivity() as AppCompatActivity
        sharedPreferences =
            activity.getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)

        if (activity is SettingsActivity) {
            textView.text = "Settings"
        } else if (activity is AlertsActivity) {
            textView.text = "Set Alerts"
        } else if (activity is CatalogActivity) {
            textView.text = "Manage Catalog"
        } else if (activity is PoseActivity) {
            textView.text = "Create / Edit Pose"
        } else if (activity is RoutineActivity) {
            textView.text = "Plan Routine"
        } else if (activity is SessionActivity) {
            textView.text = "Your Yoga Session"
        } else if (activity is ProgressActivity) {
            textView.text = "Your Progress!"
        }

        settingBtn.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadAvatar(username: String) {
        val db = AppDatabase.getDatabase(requireContext()).appDao()

        CoroutineScope(Dispatchers.IO).launch {
            val avatarArr = db.getAvatar(username)
            val bitmap = avatarArr?.let { DataUtils.byteArrayToBitmap(it) }
            withContext(Dispatchers.Main) {
                settingBtn.setImageBitmap(bitmap)
            }
        }
    }
}