package com.vyvyvi.yogabook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.SessionItem
import com.vyvyvi.yogabook.utils.InternalStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionAdapter(var ctx: Context, var resources: Int, var items: List<SessionItem>) :
    ArrayAdapter<SessionItem>(ctx, resources, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(ctx)
        val view: View = layoutInflater.inflate(resources, null)

        val img: ImageView = view.findViewById(R.id.image)
        val name: TextView = view.findViewById(R.id.englishName)
        val hindiName: TextView = view.findViewById(R.id.hindiName)
        val duration: TextView = view.findViewById(R.id.duration)

        val mItem = items[position]

        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = InternalStorageHelper.loadImageFromInternalStorage(ctx, mItem.imgFilename)
            withContext(Dispatchers.Main) {
                img.setImageBitmap(bitmap)
            }
        }
        name.text = mItem.name
        hindiName.text = mItem.hindiName
        duration.setText("${mItem.duration} s")

        return view
    }

}