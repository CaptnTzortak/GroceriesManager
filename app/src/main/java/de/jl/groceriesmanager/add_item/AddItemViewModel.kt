package de.jl.groceriesmanager.add_item

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.jl.groceriesmanager.database.item.Item
import de.jl.groceriesmanager.database.item.ItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddItemViewModel(private val itemId: Long = 0L, val database: ItemDao) : ViewModel() {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var newItem = MutableLiveData<Item?>()

    val addButtonVisible = Transformations.map(newItem) {
        neccessaryInfosAdded(it)
    }

    private fun neccessaryInfosAdded(newItem: Item?): Boolean {
        return !newItem?.itemId?.equals(0)!!
    }

    private val _navigateToInventory = MutableLiveData<Item>()
    val navigateToIntventory: LiveData<Item>
        get() = _navigateToInventory

    fun doneNavigating() {
        _navigateToInventory.value = null
    }

    init {
        Log.i("GroceryListViewModel",  "init")
        initializeItem()
    }

    private fun initializeItem() {
        uiScope.launch {
            newItem.value = Item()
        }
    }

    fun btnAddClicked(){
        uiScope.launch{
            val oldItem = newItem.value ?: return@launch
            _navigateToInventory.value = oldItem
            doneNavigating()
        }
    }
}