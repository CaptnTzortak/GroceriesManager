package de.jl.groceriesmanager.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class InventoryViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>) : T {
        if(modelClass.isAssignableFrom(InventoryViewModel::class.java)){
            return InventoryViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}