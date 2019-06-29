package com.jkjk.quicknote.taskeditscreen

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.jkjk.quicknote.R
import kotlinx.android.synthetic.main.item_location_result.view.*

/**
 *Created by chrisyeung on 6/6/2019.
 */

class LocationAdapter(private val act: AppCompatActivity,
                      private val onLocationSelectListener: OnLocationSelectListener) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    var locationList = listOf<String>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var location: String? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onLocationSelectListener.onLocationSelect(location)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(act.layoutInflater.inflate(R.layout.item_location_result, parent, false))
    }

    override fun getItemCount(): Int {
        return locationList.size + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.location = null
            holder.itemView.txtLocation.setText(R.string.my_location)
            holder.itemView.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.my_location, 0)
        } else {
            holder.location = locationList[position - 1]
            holder.itemView.txtLocation.text = holder.location
            holder.itemView.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }

        holder.itemView.vDivider.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
    }

    interface OnLocationSelectListener {
        fun onLocationSelect(location: String?)
    }
}
