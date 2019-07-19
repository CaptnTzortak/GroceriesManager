package de.jl.groceriesmanager.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*

class InventoryViewModel(val database: InventoryDao, val prodDataBase: ProductsDao, application: Application) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val inventoryItems = database.getAllInventoryItems()


    private val _navigateToAddProduct = MutableLiveData<Boolean>()
    val navigateToAddProduct: LiveData<Boolean>
        get() = _navigateToAddProduct

    private suspend fun getProductById(prodId: Long): ProductItem{
        return withContext(Dispatchers.IO){
            val prod = prodDataBase.getProductById(prodId)
            prod
        }
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

    fun insertNewInventoryItem(productId: Long){
        uiScope.launch {
            val product = getProductById(productId)
            val invItem = InventoryItem()
            invItem.product = product
            insert(invItem)
        }
    }
}