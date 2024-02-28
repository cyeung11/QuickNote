package com.jkjk.quicknote.taskeditscreen

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.jkjk.quicknote.R
import com.jkjk.quicknote.databinding.ItemLocationResultBinding

/**
 *Created by chrisyeung on 6/6/2019.
 */

class LocationAdapter(private val act: AppCompatActivity,
                      private val onLocationSelectListener: OnLocationSelectListener) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    var locationList = listOf<String>()

    inner class ViewHolder(val binding: ItemLocationResultBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var location: String? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onLocationSelectListener.onLocationSelect(location)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLocationResultBinding.inflate(act.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return locationList.size + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.location = null
            holder.binding.txtLocation.setText(R.string.my_location)
            holder.binding.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.my_location, 0)
        } else {
            holder.location = locationList[position - 1]
            holder.binding.txtLocation.text = holder.location
            holder.binding.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }

        holder.binding.vDivider.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
    }

    interface OnLocationSelectListener {
        fun onLocationSelect(location: String?)
    }
}
