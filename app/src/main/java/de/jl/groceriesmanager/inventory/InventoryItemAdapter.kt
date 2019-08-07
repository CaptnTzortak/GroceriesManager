package de.jl.groceriesmanager.inventory

import android.annotation.SuppressLint
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
import de.jl.groceriesmanager.database.inventory.Inventory
import de.jl.groceriesmanager.databinding.ItemInventoryBinding

class InventoryItemAdapter(val clickListener: InventoryItemListener) :
    ListAdapter<Inventory, InventoryItemAdapter.ViewHolder>(InventoryItemDiffCallback()) {

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
            item: Inventory,
            clickListener: InventoryItemListener,
            view: View
        ) {
            binding.inventoryProduct = item
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
            if (binding.inventoryProduct?.id != null) {
                id = binding.inventoryProduct?.id!!.toInt()
            }
            menu.add(id, 121, 0, "Delete")
        }
    }
}

class InventoryItemDiffCallback : DiffUtil.ItemCallback<Inventory>() {

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Inventory, newItem: Inventory): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Inventory, newItem: Inventory): Boolean {
        return oldItem.id == newItem.id
    }

}

class InventoryItemListener(val clickListener: (inventory_id: Long) -> Unit) {

    fun onClick(item: Inventory) = clickListener(item.id)
}