package com.vyvyvi.yogabook

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.vyvyvi.yogabook.adapters.CatalogRecyclerViewAdapter
import com.vyvyvi.yogabook.adapters.RoutineRecyclerViewAdapter
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Pose
import com.vyvyvi.yogabook.data.Routine
import com.vyvyvi.yogabook.data.RoutineWithPose
import com.vyvyvi.yogabook.utils.Configuration
import com.vyvyvi.yogabook.utils.DataUtils
import com.vyvyvi.yogabook.utils.DialogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoutineActivity : AppCompatActivity(), RoutineRecyclerViewAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineRecyclerViewAdapter
    private lateinit var addPoseFAB: FloatingActionButton
    private lateinit var items: MutableList<RoutineWithPose>

    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        recyclerView = findViewById(R.id.routineRecyclerView)
        addPoseFAB = findViewById(R.id.addPoseBtn)

        lifecycleScope.launch {
            loadData()
            initAdapter()
        }

        addPoseFAB.setOnClickListener {
            showCatalog()
        }
    }

    fun initAdapter() {
        adapter = RoutineRecyclerViewAdapter(
            this@RoutineActivity,
            items, this@RoutineActivity
        )

        recyclerView.layoutManager = LinearLayoutManager(this@RoutineActivity)
        recyclerView.adapter = adapter

        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val deletedPose: RoutineWithPose =
                        items.get(position)
                    items.removeAt(position)
                    removePoseFromCatalog(deletedPose.rid)
                    adapter!!.notifyItemRemoved(position)
                    Snackbar.make(recyclerView, "Deleted ${deletedPose.name}", Snackbar.LENGTH_LONG)
                        .setAction("Undo",
                            View.OnClickListener {
                                addPoseToCatalog(deletedPose.id, position)
                            }).show()
                }
            }).attachToRecyclerView(recyclerView)
    }

    private fun addPoseToCatalog(pose_id: Long, position: Int? = null) {
        val db = AppDatabase.getDatabase(applicationContext).appDao()
        CoroutineScope(Dispatchers.IO).launch {
            username?.let {
                val routine = Routine(username = username!!, pose_id = pose_id, duration = 30)
                val rid = db.insertRoutine(
                    routine
                )
                val poseData = db.getRoutineWithPoseById(rid)

                withContext(Dispatchers.Main) {
                    if (position != null) {
                        items.add(position, poseData)
                        adapter!!.notifyItemInserted(position)
                    } else {
                        items.add(poseData)
                        adapter.notifyItemInserted(items.size - 1)
                    }

                }
            }
        }
    }

    private fun removePoseFromCatalog(rid: Long) {
        val db = AppDatabase.getDatabase(applicationContext).appDao()
        CoroutineScope(Dispatchers.IO).launch {
            username?.let {
                db.deleteRoutine(rid)
            }
        }
    }

    private fun showCatalog() {
        val dialogView =
            LayoutInflater.from(applicationContext).inflate(R.layout.catalog_dialog, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.dialog_recyclerview)
        val db = AppDatabase.getDatabase(applicationContext).appDao()

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            val poses = db.getAllPoses()
            val listener = object : CatalogRecyclerViewAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    Snackbar.make(
                        recyclerView,
                        "Adding ${poses[position].name} to Routine...",
                        Snackbar.LENGTH_LONG
                    )
                    addPoseToCatalog(poses[position].id)
                    dialog.dismiss()
                }
            }

            val adapter = CatalogRecyclerViewAdapter(
                this@RoutineActivity,
                poses, listener
            )
            recyclerView.layoutManager = LinearLayoutManager(this@RoutineActivity)
            recyclerView.adapter = adapter
        }

    }

    private suspend fun loadData() {
        val sharedPreferences =
            getSharedPreferences(Configuration.sharedPreferences, Context.MODE_PRIVATE)
        username = sharedPreferences.getString(Configuration.userIdKey, null)

        if (username == null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        } else {
            val db = AppDatabase.getDatabase(applicationContext).appDao()
            val poses = db.getRoutineWithPose(username!!)
            items = poses.toMutableList()
        }
    }

    override fun onItemClick(position: Int) {
        val callback = { i: Int ->
            val mItem = items[position]
            mItem.duration = i
            adapter.notifyItemChanged(position)

            val db = AppDatabase.getDatabase(applicationContext).appDao()
            CoroutineScope(Dispatchers.IO).launch {
                username?.let {
                    db.updateRoutine(
                        Routine(
                            id = mItem.rid,
                            username = username!!,
                            duration = i,
                            pose_id = mItem.id
                        )
                    )
                }
            }
        }

        DialogUtil.showNumberAdjustmentDialog(
            this,
            items[position].duration,
            callback
        )
    }

    override fun onItemLongClick(position: Int) {
        val mItem = items[position]
        val i = Intent(this@RoutineActivity, PoseActivity::class.java)

        if (mItem != null) {
            i.putExtra("id", mItem.id)
            i.putExtra("name", mItem.name)
            i.putExtra("hindiName", mItem.hindiName)
            i.putExtra("imageFilename", mItem.imageFilename)
            i.putExtra("description", mItem.description)
        }
        startActivity(i)
    }
}