package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpass.databinding.PassFragmentBinding

class PasswordAdapter(
    private val items: ArrayList<Pair<String, String>>,
    private val quality: ArrayList<String>,
    private val tags: ArrayList<String>,
    private val group: ArrayList<String>,
    private val desc: ArrayList<String>,
    private val useAnalyze: String?,
    private val cardRadius: String?,
    private val metrics: DisplayMetrics?,
    val context: Context,
    val clickListener: (Int) -> Unit,
    val longClickListener: (Int, View) -> Unit,
    val tagsClickListener: (String) -> Unit,
): RecyclerView.Adapter<ViewHolder>()
{

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PassFragmentBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
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
            chip.setOnClickListener {
                tagsClickListener("2FA")
            }
            holder.group.addView(chip)
        }
        if(desc[position]!="")
            holder.passDesc.visibility = View.VISIBLE
            holder.passDesc.text = desc[position]
        if(group[position] == "#favorite"){
            holder.favorite.visibility = View.VISIBLE
        }
        if(tags[position] != "")
            tags[position] .split("\\s".toRegex()).forEach { item ->
                val chip = Chip(holder.group.context)
                chip.text= item
                chip.isClickable = true
                chip.textSize = 12F
                chip.setOnClickListener {
                    tagsClickListener(item)
                }
                holder.group.addView(chip)
            }
        when {
            quality[position] == "1" -> {
                holder.marker.setImageResource(R.drawable.circle_positive_fill)
                //holder.passFrag.setBackgroundResource(R.drawable.gradient_pos)
            }
            quality[position] == "2" -> {
                holder.marker.setImageResource(R.drawable.circle_negative_fill)
                //holder.passFrag.setBackgroundResource(R.drawable.gradient_neg)
            }
            quality[position] == "3" -> {
                holder.marker.setImageResource(R.drawable.circle_improvement_fill)
                //holder.passFrag.setBackgroundResource(R.drawable.gradient_med)
            }
            quality[position] == "4" -> {
                holder.credit.visibility = View.VISIBLE
                holder.marker.visibility = View.GONE
            }
            quality[position] == "5" -> {
                holder.creditNeg.visibility = View.VISIBLE
                holder.marker.visibility = View.GONE
            }
            quality[position] == "6" -> {
                holder.lock.visibility = View.VISIBLE
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

        if (useAnalyze != null)
            if (useAnalyze != "none"){
                holder.marker.visibility = View.GONE
                holder.credit.visibility = View.GONE
                holder.creditNeg.visibility = View.GONE
            }

        if(cardRadius != null)
            if(cardRadius != "none") {
                holder.clickableView.radius = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cardRadius.toFloat(),
                        metrics
                )
            }
    }
}

class ViewHolder(binding: PassFragmentBinding) : RecyclerView.ViewHolder(binding.root) {
    val passText = binding.listTitle
    val passDesc = binding.listDesc
    val chip = binding.chip
    // // val tags = view.tags!!
    val favorite = binding.favorite
    val clickableView = binding.clickableView
    val marker = binding.marker
    val group = binding.group
    val credit = binding.credit
    val creditNeg = binding.credit2
    val lock = binding.lock
}