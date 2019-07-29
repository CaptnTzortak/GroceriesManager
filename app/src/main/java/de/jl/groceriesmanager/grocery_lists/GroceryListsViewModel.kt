package de.jl.groceriesmanager.grocery_lists

import android.app.AlertDialog
import android.app.Application
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*

class GroceryListsViewModel(application: Application, private val glDao: GroceryListsDao) :
    AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    val groceryLists = glDao.getAllGroceryLists()


    private val _openGroceryList = MutableLiveData<Long>()
    val openGroceryList: LiveData<Long>
        get() = _openGroceryList

    private val _newGroceryListDescription = MutableLiveData<String>()
    val newGroceryListDescription: LiveData<String>
        get() = _newGroceryListDescription

    private val _newGroceryList = MutableLiveData<Long>()
    val newGroceryList: LiveData<Long>
        get() = _newGroceryList


    fun insertNewGroceryList() {
        uiScope.launch {
            val desc = _newGroceryListDescription.value.toString()
            insertNewGroceryList(desc)
        }
    }

    private suspend fun insertNewGroceryList(desc: String){
        withContext(Dispatchers.IO){
            glDao.insert(GroceryList(0L, desc))
        }
    }

    fun newGroceryList(desc: String){
        uiScope.launch {
            _newGroceryListDescription.value = desc
        }
    }

    fun deleteGroceryList(id: Long) {
        uiScope.launch {
            removeGroceryList(id)
        }
    }

    private suspend fun removeGroceryList(id: Long) {
        withContext(Dispatchers.IO){
            glDao.remove(id)
        }
    }

    fun openGroceryList(glId: Long) {
        _openGroceryList.value = glId
    }

    fun doneOpenGroceryList() {
        _openGroceryList.value = null
    }
}