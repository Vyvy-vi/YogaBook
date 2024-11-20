package com.vyvyvi.yogabook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.Pose
import com.vyvyvi.yogabook.utils.InternalStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogRecyclerViewAdapter(var ctx: Context, var items: List<Pose>,
                                 private val listener: OnItemClickListener, var viewType: Int = 0) :
    RecyclerView.Adapter<CatalogRecyclerViewAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    class ViewHolder(var v: View) : RecyclerView.ViewHolder(v) {
        var img = v.findViewById<ImageView>(R.id.image)
        var hindiName = v.findViewById<TextView>(R.id.hindiName)
        var englishName = v.findViewById<TextView>(R.id.englishName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)

        val v: View = if (viewType == 0)
            layoutInflater.inflate(R.layout.catalog_recycler_item, parent, false)
        else
            layoutInflater.inflate(R.layout.catalog_recycler_grid_item, parent, false)
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

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }
}