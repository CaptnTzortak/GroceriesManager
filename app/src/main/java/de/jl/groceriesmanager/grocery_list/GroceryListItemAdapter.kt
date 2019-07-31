package de.jl.groceriesmanager.grocery_list

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
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts
import de.jl.groceriesmanager.databinding.ItemGroceryListBinding

class GroceryListItemAdapter(val clickListener: GroceryListItemListener) :
    ListAdapter<GroceryListsProducts, GroceryListItemAdapter.ViewHolder>(GroceryListItemDiffCallback()) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val view = holder.itemView
        if(item.bought){
            view.setBackgroundColor(Color.RED)
        } else{
            view.setBackgroundColor(Color.GREEN)
        }

        holder.bind(item, clickListener, view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemGroceryListBinding) : RecyclerView.ViewHolder(binding.root),
        OnCreateContextMenuListener {

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(
            item: GroceryListsProducts,
            clickListener: GroceryListItemListener,
            view: View
        ) {
            binding.groceryListsProducts = item
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
            val glProducts = binding.groceryListsProducts
            if(glProducts != null){
                val id = glProducts.id.toInt()
                if(glProducts.bought){
                    menu.add(id, 122, 0 ,"Add to Inventory")
                }
                menu.add(id, 121, 0, "Delete")
            }

        }

        fun getItem(): GroceryListsProducts? {
            return binding.groceryListsProducts
        }
    }
}

class GroceryListItemDiffCallback : DiffUtil.ItemCallback<GroceryListsProducts>() {

    override fun areContentsTheSame(oldItem: GroceryListsProducts, newItem: GroceryListsProducts): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: GroceryListsProducts, newItem: GroceryListsProducts): Boolean {
        return oldItem.id == newItem.id
    }

}

class GroceryListItemListener(val clickListener: (groceryListsProduct: GroceryListsProducts) -> Unit) {

    fun onClick(item: GroceryListsProducts) = clickListener(item)
}