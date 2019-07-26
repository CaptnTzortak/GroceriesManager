package de.jl.groceriesmanager.grocery_list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.inventory.InventoryViewModel
import java.lang.IllegalArgumentException

class GroceryListViewModelFactory(private val glItemMappingDB: GLItemMappingDao, private val glDB: GroceryListsDao, private val productsDB: ProductsDao, private val application: Application, private val glId: Long) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroceryListViewModel::class.java)) {
            return GroceryListViewModel(glItemMappingDB, glDB, productsDB, application, glId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}