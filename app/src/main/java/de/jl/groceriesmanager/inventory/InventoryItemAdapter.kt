package de.jl.groceriesmanager.inventory

import android.graphics.Color
import android.os.Build
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.databinding.ItemInventoryBinding

class InventoryItemAdapter(val clickListener: InventoryItemListener) :
    ListAdapter<InventoryItem, InventoryItemAdapter.ViewHolder>(InventoryItemDiffCallback()) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val view = holder.itemView
        holder.bind(item, clickListener, view)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root),
        OnCreateContextMenuListener {

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(
            item: InventoryItem,
            clickListener: InventoryItemListener,
            view: View
        ) {
            binding.inventoryItem = item
            binding.clickListener = clickListener
            view.setOnCreateContextMenuListener(this)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemInventoryBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, view: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            var id = 0
            if (binding.inventoryItem?.inventory_id != null) {
                id = binding.inventoryItem?.inventory_id!!.toInt()
            }
            menu.add(id, 121, 0, "Delete")
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

class InventoryItemListener(val clickListener: (inventory_id: Long) -> Unit) {

    fun onClick(item: InventoryItem) = clickListener(item.inventory_id)
}