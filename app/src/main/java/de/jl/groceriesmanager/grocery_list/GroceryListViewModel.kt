package de.jl.groceriesmanager.grocery_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GroceryListViewModel(val database: GLItemMappingDao, application: Application) :
    AndroidViewModel(application) {

    private var groceryListId: Long = 0L

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val glItemMappingList = database.getAllProductsInGL(groceryListId)
    val groceryList = database.getGroceryListById(groceryListId)
    val groceryListProducts = database.getProductsListByGroceryListId(groceryListId)


    private val _glName = MutableLiveData<String>()
    val glName: LiveData<String>
        get() = _glName

    private val _glId = MutableLiveData<Long>()
    val glId: LiveData<Long>
        get() = _glId

    fun setGlId(id: Long){
        uiScope.launch {
            _glId.value = id
        }
    }

    fun reloadData() {
        groceryListId = glId.value!!
    }

    fun openGroceryListEntry(it: Long) {
        uiScope.launch {

        }

    }

    fun refreshGroceryListDetails() {
        uiScope.launch {
            _glName.value = groceryList.value?.description
        }
    }
}