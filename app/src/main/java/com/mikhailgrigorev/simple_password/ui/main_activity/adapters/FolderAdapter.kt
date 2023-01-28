package com.mikhailgrigorev.simple_password.ui.main_activity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikhailgrigorev.simple_password.data.dbo.FolderCard
import com.mikhailgrigorev.simple_password.databinding.ItemFolderCardBinding

class FolderAdapter(
    private val folders: List<FolderCard>,
    val context: Context,
    val clickListener: (Int) -> Unit,
    val longClickListener: (Int, View) -> Unit,
) : RecyclerView.Adapter<FolderViewHolder>() {

    override fun getItemCount(): Int {
        return folders.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return FolderViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.folderName.text = folder.name
        holder.folderDescription.text = folder.description

        if(folder.colorTag != ""){
            holder.cvColorCard.visibility = View.VISIBLE
            holder.cvColorCard.setCardBackgroundColor(Color.parseColor(folder.colorTag))
        }

        holder.folderCard.setOnClickListener {
            clickListener(position)
        }
        holder.folderCard.setOnLongClickListener {
            longClickListener(position, it)
            return@setOnLongClickListener (true)
        }
    }
}

class FolderViewHolder(binding: ItemFolderCardBinding) : RecyclerView.ViewHolder(binding.root) {
    val folderName = binding.tvFolderName
    val folderDescription = binding.tvDescription
    val cvColorCard = binding.cvColorCard
    val folderCard = binding.cvFolderCard
}