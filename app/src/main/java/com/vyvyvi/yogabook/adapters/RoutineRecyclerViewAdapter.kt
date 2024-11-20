package com.vyvyvi.yogabook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.RoutineWithPose
import com.vyvyvi.yogabook.utils.InternalStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoutineRecyclerViewAdapter(var ctx: Context, var items: List<RoutineWithPose>,
                                 private val listener: OnItemClickListener) :
    RecyclerView.Adapter<RoutineRecyclerViewAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var img = v.findViewById<ImageView>(R.id.image)
        var hindiName = v.findViewById<TextView>(R.id.hindiName)
        var englishName = v.findViewById<TextView>(R.id.englishName)
        val duration = v.findViewById<TextView>(R.id.duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val v: View = layoutInflater.inflate(R.layout.routine_recycler_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mItem = items[position]
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = InternalStorageHelper.loadImageFromInternalStorage(ctx, mItem.imageFilename)
            withContext(Dispatchers.Main) {
                holder.img.setImageBitmap(bitmap)
            }
        }
        holder.englishName.text  = mItem.name
        holder.hindiName.text = mItem.hindiName
        holder.duration.text = mItem.duration.toString() + " s"

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }

        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(position)
            true
        }
    }
}