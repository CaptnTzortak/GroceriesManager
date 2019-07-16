package de.jl.groceriesmanager.inventory

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.inventory.InventoryItem
import kotlinx.coroutines.*

class InventoryViewModel(val database: InventoryDao, application: Application) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val inventoryItems = database.getAllInventoryItems()

    private var inventoryItem = MutableLiveData<InventoryItem?>()

    init {

    }


    fun onAddInventoyItem() {
        uiScope.launch {
            val newInventoyItem = InventoryItem()
            insert(newInventoyItem)
            inventoryItem.value = newInventoyItem
        }
    }


    private suspend fun insert(item: InventoryItem){
        withContext(Dispatchers.IO) {
            database.insert(item)
        }
    }

}