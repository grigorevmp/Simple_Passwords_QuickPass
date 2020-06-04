package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pass_fragment.view.*

class PasswordAdapter(private val items : ArrayList<Pair<String, String>>, private val quality : ArrayList<String>, val context: Context, val clickListener: (Int) -> Unit) : RecyclerView.Adapter<ViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.pass_fragment, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.passText.text = items[position].first
        if(items[position].second == "0"){
            holder.chip.visibility = View.GONE
        }
        when {
            quality[position] == "1" -> {
                holder.marker.setImageResource(R.drawable.circle_positive)
            }
            quality[position] == "2" -> {
                holder.marker.setImageResource(R.drawable.circle_negative)
            }
            quality[position] == "3" -> {
                holder.marker.setImageResource(R.drawable.circle_improvement)
            }
        }
        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val passText = view.list_title!!
    val chip = view.chip!!
    val clickableView = view.clickable_view!!
    val marker = view.marker!!
}