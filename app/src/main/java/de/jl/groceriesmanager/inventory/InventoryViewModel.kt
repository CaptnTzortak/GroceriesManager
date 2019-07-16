package de.jl.groceriesmanager.inventory

import android.app.Application
import android.content.ClipData
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.inventory.InventoryDao
import kotlinx.coroutines.*

class InventoryViewModel(val database: InventoryDao, application: Application) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val inventoryItems = database.getAllInventoryItems()



    //Inventar-Liste

    private val _navigateToAddItem = MutableLiveData<Boolean>()
    val navigateToAddItem: LiveData<Boolean>
        get() = _navigateToAddItem


    init {
        Log.i("InventoryViewModel",  "init")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private suspend fun insert() {
        withContext(Dispatchers.IO) {
        }
    }

    fun btnAddClicked(){
        uiScope.launch{
            _navigateToAddItem.value = true
        }
    }

    fun doneNavigatingToAddItem() {
        _navigateToAddItem.value = false
    }
}