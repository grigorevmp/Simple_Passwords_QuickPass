package com.mikhailgrigorev.quickpassword.ui.main_activity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ItemPasswordCardBinding

class PasswordAdapter(
    private val passwordCards: List<PasswordCard>,
    val context: Context,
    val clickListener: (Int) -> Unit,
    val longClickListener: (Int, View) -> Unit,
    val tagsClickListener: (String) -> Unit,
): RecyclerView.Adapter<ViewHolder>()
{

    override fun getItemCount(): Int {
        return passwordCards.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPasswordCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val password = passwordCards[position]
        holder.passwordName.text = password.name

        if (password.use_2fa) {
            val chip = Chip(holder.passwordGroup.context)
            chip.text = "2FA"
            chip.isClickable = false
            chip.alpha = 0.7f
            chip.textSize = 12F
            holder.passwordGroup.addView(chip)
        }
        if (password.description != "")
            holder.passwordDescription.visibility = View.VISIBLE
        holder.passwordDescription.text = password.description
        if (password.favorite) {
            holder.favoriteButton.visibility = View.VISIBLE
        }
        if (password.tags != "")
            password.tags.split("\\s".toRegex()).forEach { item ->
                val chip = Chip(holder.passwordGroup.context)
                chip.text = item
                chip.isClickable = true
                chip.textSize = 12F
                chip.setOnClickListener {
                    tagsClickListener(item)
                }
                holder.passwordGroup.addView(chip)
            }
        if(password.encrypted){
            holder.lockIcon.visibility = View.VISIBLE
            holder.qualityMarker.visibility = View.GONE
        }
        when (password.quality) {
            1 -> {
                holder.qualityMarker.setImageResource(R.drawable.circle_positive_fill)
                holder.lockIcon.setColorFilter(context.getColor(R.color.green_quality))
            }
            2 -> {
                holder.qualityMarker.setImageResource(R.drawable.circle_negative_fill)
                holder.lockIcon.setColorFilter(context.getColor(R.color.red_quality))
            }
            3 -> {
                holder.qualityMarker.setImageResource(R.drawable.circle_improvement_fill)
                holder.lockIcon.setColorFilter(context.getColor(R.color.yellow_quality))
            }
            4 -> {
                holder.creditCard.visibility = View.VISIBLE
                holder.qualityMarker.visibility = View.GONE
            }
            5 -> {
                holder.creditCardNeg.visibility = View.VISIBLE
                holder.qualityMarker.visibility = View.GONE
            }
        }
        holder.passwordCard.setOnClickListener {
            clickListener(position)
        }
        holder.passwordCard.setOnLongClickListener {
            longClickListener(position, it)
            return@setOnLongClickListener (true)
        }

            if (!Utils.useAnalyze()) {
                holder.qualityMarker.visibility = View.GONE
                holder.creditCard.visibility = View.GONE
                holder.creditCardNeg.visibility = View.GONE
            }
    }
}

class ViewHolder(binding: ItemPasswordCardBinding) : RecyclerView.ViewHolder(binding.root) {
    val passwordName = binding.tvPasswordName
    val passwordDescription = binding.tvDescription
    val favoriteButton = binding.ibFavorite
    val passwordCard = binding.cvCard
    val qualityMarker = binding.ivMarker
    val passwordGroup = binding.cgPasswordChipGroup
    val creditCard = binding.ivCredit
    val creditCardNeg = binding.ivCreditAlt
    val lockIcon = binding.ivLock
}