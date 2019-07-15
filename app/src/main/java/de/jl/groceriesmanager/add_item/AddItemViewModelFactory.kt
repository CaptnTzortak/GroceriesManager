package de.jl.groceriesmanager.add_item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.database.item.ItemDao

class AddItemViewModelFactory(
    private val itemId: Long,
    private val dataSource: ItemDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
            return AddItemViewModel(itemId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}