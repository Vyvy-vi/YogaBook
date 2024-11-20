package com.vyvyvi.yogabook.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.vyvyvi.yogabook.AlertsActivity
import com.vyvyvi.yogabook.CatalogActivity
import com.vyvyvi.yogabook.MainActivity
import com.vyvyvi.yogabook.ProgressActivity
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.RoutineActivity

data class MenuOption(val title: String, val img: Int, val activity: Class<*>)

val items = arrayOf(
    MenuOption("Plan Routine", R.drawable.ic_routine, RoutineActivity::class.java),
    MenuOption("View Progress", R.drawable.ic_progress, ProgressActivity::class.java),
    MenuOption("Manage Catalog", R.drawable.ic_catalog, CatalogActivity::class.java),
    MenuOption("Set Alerts", R.drawable.ic_alerts, AlertsActivity::class.java)
)
class MenuGridAdapter(context : Context) :
    ArrayAdapter<MenuOption>(context,0,items) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(R.layout.card_layout,null)
        val titleText = view.findViewById<TextView>(R.id.card_text)
        val imageView = view.findViewById<ImageView>(R.id.card_image)

        val mItem = items[position]

        titleText.text = mItem.title
        imageView.setImageDrawable(context.resources.getDrawable(mItem.img))
        return view
    }
}