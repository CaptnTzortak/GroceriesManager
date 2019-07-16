package de.jl.groceriesmanager.grocerylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class GroceryListViewModelFactory : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>) : T {
        if(modelClass.isAssignableFrom(GroceryListViewModel::class.java)){
            return GroceryListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}