package com.mikhailgrigorev.quickpassword.ui.main_activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.data.entity.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.PasswordCardBinding

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
        val binding = PasswordCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val password = passwordCards[position]
        holder.passwordName.text = password.name

        holder.chip2FA.visibility = View.GONE
        if (password.use_2fa) {
            val chip = Chip(holder.passwordGroup.context)
            chip.text = "2FA"
            chip.isClickable = false
            chip.textSize = 12F
            chip.setOnClickListener {
                tagsClickListener("2FA")
            }
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
        when (password.quality) {
            1 -> {
                holder.qualityMarker.setImageResource(R.drawable.circle_positive_fill)
            }
            2 -> {
                holder.qualityMarker.setImageResource(R.drawable.circle_negative_fill)
            }
            3 -> {
                holder.qualityMarker.setImageResource(R.drawable.circle_improvement_fill)
            }
            4 -> {
                holder.creditCard.visibility = View.VISIBLE
                holder.qualityMarker.visibility = View.GONE
            }
            5 -> {
                holder.creditCardNeg.visibility = View.VISIBLE
                holder.qualityMarker.visibility = View.GONE
            }
            6 -> {
                holder.lockIcon.visibility = View.VISIBLE
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

        if (Utils.useAnalyze() != null)
            if (Utils.useAnalyze() != "none") {
                holder.qualityMarker.visibility = View.GONE
                holder.creditCard.visibility = View.GONE
                holder.creditCardNeg.visibility = View.GONE
            }
    }
}

class ViewHolder(binding: PasswordCardBinding) : RecyclerView.ViewHolder(binding.root) {
    val passwordName = binding.tvPasswordName
    val passwordDescription = binding.tvDescription
    val chip2FA = binding.c2FAChip
    val favoriteButton = binding.ibFavorite
    val passwordCard = binding.cvCard
    val qualityMarker = binding.ivMarker
    val passwordGroup = binding.cgPasswordChipGroup
    val creditCard = binding.ivCredit
    val creditCardNeg = binding.ivCreditAlt
    val lockIcon = binding.ivLock
}