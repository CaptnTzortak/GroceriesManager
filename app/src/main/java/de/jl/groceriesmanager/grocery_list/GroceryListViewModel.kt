package de.jl.groceriesmanager.grocery_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.gl_item_mapping.GLItemMapping
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*

class GroceryListViewModel(val glItemMappingDatabase: GLItemMappingDao, val glDatabase: GroceryListsDao, val productDatabase: ProductsDao, application: Application, val glId: Long) :
    AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _addProduct = MutableLiveData<Long>()
    val addProduct : LiveData<Long>
        get()=_addProduct

    val glItemMappingList = glItemMappingDatabase.getItemMappingByGroceryListId(glId)
    val glName = MutableLiveData<String>()

    private val _existingGlId = MutableLiveData<Long>()
    val existingGlId: LiveData<Long>
        get() = _existingGlId

    init {
        if(glId > 0) {
            fillGroceryList(glId)
            _existingGlId.value = glId
        }
    }

    private fun fillGroceryList(id: Long){
        uiScope.launch {
            val existingGL = getGroceryListById(id)
            glName.value = existingGL.description
        }
    }

    private suspend fun getGroceryListById(id: Long): GroceryList {
        return withContext(Dispatchers.IO){
            val gl = glDatabase.getGroceryListById(id)
            gl
        }
    }

    private suspend fun insertNewGroceryListItemMapping(prodId: Long, note: String){
        withContext(Dispatchers.IO){
            var itemMapping = GLItemMapping()
            itemMapping.groceryList = existingGlId.value?.let { getGroceryListById(it) }
            itemMapping.note = note
            itemMapping.product = getProductById(prodId)
            glItemMappingDatabase.upsert(itemMapping)
        }
    }

    private suspend fun getProductById(prodId: Long): ProductItem? {
        return withContext(Dispatchers.IO){
            val product = productDatabase.getProductById(prodId)
            product
        }
    }

    fun addProductClicked(){
        _addProduct.value = 0L
    }

    fun doneNavigatingToAddProductGL() {
        _addProduct.value = null
    }

    fun newProductInserted(productId: Long, note: String) {
        uiScope.launch {
            insertNewGroceryListItemMapping(productId, note)
        }
    }

    fun deleteGroceryListEntry(id: Long) {
        uiScope.launch {
            removeGLItemMapping(id)
        }

    }

    private suspend fun removeGLItemMapping(id: Long) {
        withContext(Dispatchers.IO){
            glItemMappingDatabase.remove(id)
        }

    }
}