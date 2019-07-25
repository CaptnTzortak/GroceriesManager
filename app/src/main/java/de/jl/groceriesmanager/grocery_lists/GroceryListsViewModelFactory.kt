package de.jl.groceriesmanager.grocery_lists

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import java.lang.IllegalArgumentException

class GroceryListsViewModelFactory(private val dataSource: GroceryListsDao, private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>) : T {
        if(modelClass.isAssignableFrom(GroceryListsViewModel::class.java)){
            return GroceryListsViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}