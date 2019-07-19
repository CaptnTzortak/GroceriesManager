package de.jl.groceriesmanager.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.databinding.ItemInventoryBinding

class InventoryItemAdapter : ListAdapter<InventoryItem, InventoryItemAdapter.ViewHolder>(InventoryItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: InventoryItem) {
            binding.inventoryItem = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemInventoryBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class InventoryItemDiffCallback : DiffUtil.ItemCallback<InventoryItem>() {

    override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
        return oldItem.inventory_id == newItem.inventory_id
    }

}