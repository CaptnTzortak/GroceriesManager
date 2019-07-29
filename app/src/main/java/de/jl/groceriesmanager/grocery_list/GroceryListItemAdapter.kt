package de.jl.groceriesmanager.grocery_list

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
import de.jl.groceriesmanager.database.gl_item_mapping.GLItemMapping
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.databinding.ItemGroceryListBinding

class GroceryListItemAdapter(val clickListener: GroceryListItemListener) :
    ListAdapter<GLItemMapping, GroceryListItemAdapter.ViewHolder>(GroceryListItemDiffCallback()) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val view = holder.itemView
        holder.bind(item, clickListener, view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemGroceryListBinding) : RecyclerView.ViewHolder(binding.root),
        OnCreateContextMenuListener {

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(
            item: GLItemMapping,
            clickListener: GroceryListItemListener,
            view: View
        ) {
            binding.glItemMapping = item
            binding.clickListener = clickListener
            view.setOnCreateContextMenuListener(this)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGroceryListBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, view: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            var id = 0
            if (binding.glItemMapping?.gl_item_mapping_id != null) {
                id = binding.glItemMapping?.gl_item_mapping_id!!.toInt()
            }
            menu.add(id, 121, 0, "Delete")
        }

        fun getItem(): GLItemMapping? {
            return binding.glItemMapping
        }
    }
}

class GroceryListItemDiffCallback : DiffUtil.ItemCallback<GLItemMapping>() {

    override fun areContentsTheSame(oldItem: GLItemMapping, newItem: GLItemMapping): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: GLItemMapping, newItem: GLItemMapping): Boolean {
        return oldItem.gl_item_mapping_id == newItem.gl_item_mapping_id
    }

}

class GroceryListItemListener(val clickListener: (groceryList_id: Long) -> Unit) {

    fun onClick(item: GLItemMapping) = clickListener(item.gl_item_mapping_id)
}