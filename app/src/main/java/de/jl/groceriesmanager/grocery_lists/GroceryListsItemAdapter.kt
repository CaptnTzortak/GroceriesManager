package de.jl.groceriesmanager.grocery_lists

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
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import de.jl.groceriesmanager.databinding.ItemGroceryListsBinding

class GroceryListsItemAdapter(val clickListener: GroceryListsItemListener) :
    ListAdapter<GroceryList, GroceryListsItemAdapter.ViewHolder>(
        GroceryListsItemDiffCallback()
    ) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val view = holder.itemView
        holder.bind(item, clickListener, view)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemGroceryListsBinding) : RecyclerView.ViewHolder(binding.root),
        OnCreateContextMenuListener {

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(
            item: GroceryList,
            clickListener: GroceryListsItemListener,
            view: View
        ) {
            binding.groceryList = item
            binding.clickListener = clickListener
            view.setOnCreateContextMenuListener(this)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGroceryListsBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, view: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            var id = 0
            if (binding.groceryList?.id != null) {
                id = binding.groceryList?.id!!.toInt()
            }
            menu.add(id, 121, 0, "Delete")
            menu.add(id, 122, 0, "Modify")
        }
    }
}

class GroceryListsItemDiffCallback : DiffUtil.ItemCallback<GroceryList>() {

    override fun areContentsTheSame(oldItem: GroceryList, newItem: GroceryList): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: GroceryList, newItem: GroceryList): Boolean {
        return oldItem.id == newItem.id
    }

}

class GroceryListsItemListener(val clickListener: (groceryList_id: Long) -> Unit) {

    fun onClick(item: GroceryList) = clickListener(item.id)
}