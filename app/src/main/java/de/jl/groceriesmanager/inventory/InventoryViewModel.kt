package de.jl.groceriesmanager.inventory

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.jl.groceriesmanager.database.inventory.Inventory
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.item.Item
import kotlinx.coroutines.*

class InventoryViewModel(val database: InventoryDao, application: Application) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //Inventar-Liste
    private var inventory = MutableLiveData<Inventory?>()



    private val _navigateToAddItem = MutableLiveData<Item>()

    val navigateToAddItem: LiveData<Item>
        get() = _navigateToAddItem

    init {
        Log.i("InventoryViewModel",  "init")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private suspend fun insert(item: Item) {
        withContext(Dispatchers.IO) {
            database.insert(item)
        }
    }

    fun btnAddClicked(){
        uiScope.launch{
            val newItem = Item()
            _navigateToAddItem.value = newItem
        }
    }

    fun doneNavigatingToAddItem() {
        _navigateToAddItem.value = null
    }
}