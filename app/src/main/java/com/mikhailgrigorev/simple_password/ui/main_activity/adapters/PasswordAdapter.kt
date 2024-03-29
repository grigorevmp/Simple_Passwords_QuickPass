package com.mikhailgrigorev.simple_password.ui.main_activity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.utils.PasswordQuality
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.databinding.ItemPasswordCardBinding

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
        holder.passwordGroup.removeAllViews()
        holder.passwordDescription.text = password.description

        if (password.use_2fa) {
            val chip = Chip(
                    holder.passwordGroup.context,
                    null,
                    R.style.Widget_MaterialComponents_Chip_Choice
            )

            chip.text = "2FA"
            chip.isClickable = false
            chip.isChecked = false
            chip.alpha = 0.7f
            chip.textSize = 12F

            holder.passwordGroup.addView(chip)
        }

        if (password.description != "") holder.passwordDescription.visibility = View.VISIBLE

        if (password.favorite) { holder.favoriteButton.visibility = View.VISIBLE }

        if (password.tags != "")
            password.tags.trim()
                    .split("\\s".toRegex()).forEach { item ->
                    val chip = Chip(
                        holder.passwordGroup.context,
                        null,
                        R.style.Widget_MaterialComponents_Chip_Choice
                    )

                chip.text = item.trim()
                chip.isClickable = true
                chip.isChecked = false
                chip.textSize = 12F
                chip.setOnClickListener { tagsClickListener(item) }

                holder.passwordGroup.addView(chip)
            }

        when {
            password.encrypted -> {
                holder.lockIcon.visibility = View.VISIBLE
                holder.qualityMarker.visibility = View.GONE
            }
            password.is_card_pin -> {
                if (password.quality == PasswordQuality.LOW.value) {
                    holder.creditCardNeg.visibility = View.VISIBLE
                } else {
                    holder.creditCard.visibility = View.VISIBLE
                }
                holder.qualityMarker.visibility = View.GONE
            }
            else -> when (password.quality) {
                PasswordQuality.HIGH.value -> {
                    holder.qualityMarker.setImageResource(R.drawable.circle_green_fill)
                    holder.lockIcon.setColorFilter(context.getColor(R.color.green_quality))
                }
                PasswordQuality.LOW.value -> {
                    holder.qualityMarker.setImageResource(R.drawable.circle_red_fill)
                    holder.lockIcon.setColorFilter(context.getColor(R.color.red_quality))
                }
                PasswordQuality.MEDIUM.value -> {
                    holder.qualityMarker.setImageResource(R.drawable.circle_yellow_fill)
                    holder.lockIcon.setColorFilter(context.getColor(R.color.yellow_quality))
                }
            }
        }
        holder.passwordCard.setOnClickListener {
            clickListener(position)
        }

        holder.passwordCard.setOnLongClickListener {
            longClickListener(position, it)
            return@setOnLongClickListener (true)
        }

        if (!Utils.toggleManager.analyzeToggle.isEnabled()) {
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