package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.pass_fragment.view.*

class PasswordAdapter(private val items: ArrayList<Pair<String, String>>,
                      private val quality: ArrayList<String>,
                      private val tags: ArrayList<String>,
                      private val group: ArrayList<String>,
                      val context: Context,
                      val clickListener: (Int) -> Unit,
                      val longClickListener: (Int, View) -> Unit
): RecyclerView.Adapter<ViewHolder>()
{

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.pass_fragment, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.passText.text = items[position].first
        holder.chip.visibility = View.GONE
        if(items[position].second != "0"){
            val chip = Chip(holder.group.context)
            chip.text= "2FA"
            chip.isClickable = false
            chip.textSize = 12F
            holder.group.addView(chip)
        }
        if(group[position] == "#favorite"){
            holder.favorite.visibility = View.VISIBLE
        }
        if(tags[position] != "")
            tags[position] .split("\\s".toRegex()).forEach { item ->
                val chip = Chip(holder.group.context)
                chip.text= item
                chip.isClickable = false
                chip.textSize = 12F
                holder.group.addView(chip)
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
            quality[position] == "4" -> {
                holder.credit.visibility = View.VISIBLE
                holder.marker.visibility = View.GONE
            }
        }
        holder.clickableView.setOnClickListener {
            clickListener(position)
        }
        holder.clickableView.setOnLongClickListener {
            longClickListener(position, it)
            return@setOnLongClickListener (true)
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val passText = view.list_title!!
    val chip = view.chip!!
    // // val tags = view.tags!!
    val favorite = view.favorite!!
    val clickableView = view.clickable_view!!
    val marker = view.marker!!
    val group = view.group!!
    val credit = view.credit!!
}