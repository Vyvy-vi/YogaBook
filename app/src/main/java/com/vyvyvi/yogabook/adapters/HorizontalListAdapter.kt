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
import com.vyvyvi.yogabook.data.SessionItem
import com.vyvyvi.yogabook.utils.InternalStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HorizontalListAdapter(var ctx: Context, var items: List<SessionItem>,
                                 private val listener: OnItemClickListener) :
    RecyclerView.Adapter<HorizontalListAdapter.ViewHolder>() {

    private var activeItemIndex = 0

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setActiveItem(position: Int) {
        activeItemIndex = position
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var img = v.findViewById<ImageView>(R.id.image)
        var pose_name = v.findViewById<TextView>(R.id.pose_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val v: View = layoutInflater.inflate(R.layout.horizontal_pose_icon, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mItem = items[position]
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = InternalStorageHelper.loadImageFromInternalStorage(ctx, mItem.imgFilename)
            withContext(Dispatchers.Main) {
                holder.img.setImageBitmap(bitmap)
            }
        }

        holder.pose_name.text = mItem.hindiName.take(20)  + "..."

        holder.itemView.isSelected = position == activeItemIndex

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }
}