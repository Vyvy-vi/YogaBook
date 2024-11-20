package com.vyvyvi.yogabook

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vyvyvi.yogabook.adapters.CatalogRecyclerViewAdapter
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Pose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogActivity : AppCompatActivity(), CatalogRecyclerViewAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CatalogRecyclerViewAdapter
    private lateinit var toggleBtn: Button
    private lateinit var loadBtn: Button
    private lateinit var addPoseFAB: FloatingActionButton
    private lateinit var items: List<Pose>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        recyclerView = findViewById(R.id.catalogRecyclerView)
        toggleBtn = findViewById(R.id.toggleManager)
        loadBtn = findViewById(R.id.loadData)
        addPoseFAB = findViewById(R.id.addPoseBtn)

        lifecycleScope.launch {
            loadData()
            initAdapter()
        }

        addPoseFAB.setOnClickListener {
            val i = Intent(this@CatalogActivity, PoseActivity::class.java)
            startActivity(i)
            finish()
        }

        loadBtn.setOnClickListener {
            lifecycleScope.launch {
                loadData()
                adapter.items = items
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun initAdapter() {
        adapter = CatalogRecyclerViewAdapter(
            this@CatalogActivity,
            items, this@CatalogActivity
        )

        recyclerView.layoutManager = LinearLayoutManager(this@CatalogActivity)
        recyclerView.adapter = adapter


        toggleBtn.setOnClickListener {
            val newViewType = if (toggleBtn.text == "SNAP TO GRID") {
                toggleBtn.text = "SNAP TO LIST"
                recyclerView.layoutManager = GridLayoutManager(this@CatalogActivity, 2)
                1
            } else {
                toggleBtn.text = "SNAP TO GRID"
                recyclerView.layoutManager = LinearLayoutManager(this@CatalogActivity)
                0
            }
            adapter.viewType = newViewType
            adapter.notifyDataSetChanged()
        }
    }

    private suspend fun loadData() {
        val db = AppDatabase.getDatabase(applicationContext).appDao()
        val poses = db.getAllPoses()
        items = poses
    }

    override fun onItemClick(position: Int) {
        val mItem = items[position]
        val i = Intent(this@CatalogActivity, PoseActivity::class.java)

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