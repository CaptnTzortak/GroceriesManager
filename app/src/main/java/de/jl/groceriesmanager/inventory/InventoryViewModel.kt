package de.jl.groceriesmanager.inventory

import android.app.Application
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


    private val _navigateToAddProduct = MutableLiveData<Boolean>()
    val navigateToAddProduct: LiveData<Boolean>
        get() = _navigateToAddProduct

    init {

    }


    fun onAddInventoyItem() {
        uiScope.launch {
            _navigateToAddProduct.value = true
        }
    }

    fun doneNavigatingToAddProduct(){
        _navigateToAddProduct.value = false
    }

    private suspend fun insert(item: InventoryItem) {
        withContext(Dispatchers.IO) {
            database.insert(item)
        }
    }

}