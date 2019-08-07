package de.jl.groceriesmanager.grocery_lists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import kotlinx.coroutines.*

class GroceryListsViewModel(application: Application) :
    AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val glDao = GroceriesManagerDB.getInstance(application).groceryListsDao
    private val glpDao = GroceriesManagerDB.getInstance(application).groceryListsProductsDao
    private val prodDao = GroceriesManagerDB.getInstance(application).productsDao

    val groceryLists = glDao.getAllGroceryLists()

    private val _openGroceryList = MutableLiveData<Long>()
    val openGroceryList: LiveData<Long>
        get() = _openGroceryList

    private val _newGroceryListDescription = MutableLiveData<Pair<Long,String>>()
    val newGroceryListDescription: LiveData<Pair<Long,String>>
        get() = _newGroceryListDescription


    fun insertNewGroceryList() {
        uiScope.launch {
        }
    }

    private suspend fun insertNewGL() {
        withContext(Dispatchers.IO) {
        }
    }

    fun newGroceryList(pair: Pair<Long,String>) {
        uiScope.launch {
            _newGroceryListDescription.value = pair
        }
    }

    fun deleteGroceryList(id: Long) {
        uiScope.launch {
            val productIds = getAllProductIdsInGLProductsByGLId(id)
            removeGroceryListsProducts(id)
            removeGroceryList(id)
            removeProducts(productIds)
        }
    }

    private suspend fun getAllProductIdsInGLProductsByGLId(id: Long) : List<Long> {
        return withContext(Dispatchers.IO){
            glpDao.getAllProductIdsByGlId(id)
        }
    }

    private suspend fun removeGroceryList(id: Long) {
        withContext(Dispatchers.IO) {
            glDao.deleteById(id)
        }
    }

    private suspend fun removeGroceryListsProducts(id: Long) {
        withContext(Dispatchers.IO){
            glpDao.deleteAllGroceryListsProductsByGlId(id)
        }
    }

    private suspend fun removeProducts(ids: List<Long>){
        withContext(Dispatchers.IO){
            ids.iterator().forEach {
                prodDao.deleteById(it)
            }
        }
    }

    fun openGroceryList(glId: Long) {
        _openGroceryList.value = glId
    }

    fun doneOpenGroceryList() {
        _openGroceryList.value = null
    }
}