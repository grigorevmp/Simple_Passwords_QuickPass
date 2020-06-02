package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pass_fragment.view.*

class passwordAdapter(val items : ArrayList<String>, val context: Context, val clickListener: (Int) -> Unit) : RecyclerView.Adapter<ViewHolder>() {

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
        holder.passText?.text = items[position]
        // holder?.passContent?.text = items.get(position)
        holder.passContent?.text = "Tap to view password"
        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val passText = view.list_title
    val passContent = view.list_description
    val clickableView = view.clickable_view
}