package com.vyvyvi.yogabook

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vyvyvi.yogabook.adapters.SessionAdapter
import com.vyvyvi.yogabook.data.AppDao
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Session
import com.vyvyvi.yogabook.data.SessionItem
import com.vyvyvi.yogabook.utils.Configuration
import com.vyvyvi.yogabook.utils.DialogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

class SessionActivity : AppCompatActivity() {
    private var username: String? = null
    private lateinit var db: AppDao

    private lateinit var editFab: FloatingActionButton
    private lateinit var startFab: FloatingActionButton
    private lateinit var editBtn: Button

    private lateinit var sessionList: ListView
    private var sessionId: Long? = null
    private lateinit var sessionItems: List<SessionItem>
    private lateinit var adapter: SessionAdapter

    private val LOCATION_PERMISSION_REQ_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        val sharedPreferences =
            getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Configuration.userIdKey, null)

        if (username == null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        startFab = findViewById(R.id.startSession)
        editFab = findViewById(R.id.editRoutine)
        editBtn = findViewById(R.id.editBtn)
        sessionList = findViewById(R.id.sessionList)
        startFab.isEnabled = false
        editBtn.setOnClickListener {
            val intent = Intent(this, RoutineActivity::class.java)
            startActivity(intent)
        }
        editFab.setOnClickListener {
            val intent = Intent(this, RoutineActivity::class.java)
            startActivity(intent)
        }

        sessionList.setOnItemLongClickListener { parent, view, position, id ->
            val mItem = sessionItems[position]
            val i = Intent(this@SessionActivity, PoseActivity::class.java)

            if (mItem != null) {
                i.putExtra("id", mItem.id)
                i.putExtra("name", mItem.name)
                i.putExtra("hindiName", mItem.hindiName)
                i.putExtra("imageFilename", mItem.imgFilename)
            }
            startActivity(i)
            true
        }


        startFab.setOnClickListener {
            val intent = Intent(this, TrackActivity::class.java)
            intent.putExtra("session_id", sessionId)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        db = AppDatabase.getDatabase(applicationContext).appDao()
        lifecycleScope.launch {
            createSession()
        }
    }

    fun updateLocation(sessionId: Long) {
        if (permissions.toList().any {
                ActivityCompat.checkSelfPermission(this@SessionActivity, it) !=
                        PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation

                if (location == null) return
                val geocoder = Geocoder(this@SessionActivity, Locale.getDefault())
                val list: MutableList<Address>? =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (list == null) return

                val latitude = list[0].latitude
                val longitude = list[0].longitude
                val address = "${list[0].getAddressLine(0)}, ${list[0].countryName}"

                CoroutineScope(Dispatchers.IO).launch {
                    db.updateSessionLocation(sessionId, latitude, longitude, address)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createSession() {
        if (username == null) return
        val user = db.getUser(username!!)

        sessionId = db.insertSession(
            Session(
                username = username!!,
                date = LocalDate.now(),
            )
        )

        if (user.trackLocation) {
            updateLocation(sessionId!!)
        }

        sessionItems = (db.getRoutineWithPose(username!!)).map {
            SessionItem(
                sessionId = sessionId!!,
                name = it.name,
                hindiName = it.hindiName,
                duration = it.duration,
                imgFilename = it.imageFilename,
                description = it.description
            )
        }

        sessionItems.forEach {
            val itemId = db.insertSessionItem(it)
            it.id = itemId
        }

        withContext(Dispatchers.Main) {
            Log.d("SESSIONS", sessionItems.toString())
            adapter =
                SessionAdapter(applicationContext, R.layout.routine_recycler_item, sessionItems)
            sessionList.adapter = adapter
            startFab.isEnabled = true

            sessionList.setOnItemClickListener { adapterView, view, position, l ->
                val callback = { i: Int ->
                    val mItem = sessionItems[position]
                    mItem.duration = i
                    adapter.notifyDataSetChanged()
                    CoroutineScope(Dispatchers.IO).launch {
                        username?.let {
                            db.updateSessionItem(sessionItems[position])
                        }
                    }
                }
                DialogUtil.showNumberAdjustmentDialog(
                    this@SessionActivity,
                    sessionItems[position].duration,
                    callback
                )
            }
        }
    }
}