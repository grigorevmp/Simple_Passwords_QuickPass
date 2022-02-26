package com.mikhailgrigorev.quickpassword.ui.main_activity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikhailgrigorev.quickpassword.data.dbo.FolderCard
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ItemFolderBinding

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
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(context), parent, false)
        return FolderViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.folderName.text = folder.name
        holder.folderDescription.text = folder.description

        holder.folderCard.setOnClickListener {
            clickListener(position)
        }
        holder.folderCard.setOnLongClickListener {
            longClickListener(position, it)
            return@setOnLongClickListener (true)
        }
    }
}

class FolderViewHolder(binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root) {
    val folderName = binding.tvFolderName
    val folderDescription = binding.tvDescription
    val colorTag = binding.ivColorTag
    val folderCard = binding.cvFolderCard
}