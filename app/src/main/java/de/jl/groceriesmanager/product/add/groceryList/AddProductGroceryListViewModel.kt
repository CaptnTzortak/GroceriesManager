package de.jl.groceriesmanager.product.add.groceryList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*
import java.sql.Date
import java.util.*

class AddProductGroceryListViewModel(application: Application, private val prodDao: ProductsDao) : AndroidViewModel(application) {
    /** Coroutine variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()

    /**
     * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
     *
     * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
     * by calling `viewModelJob.cancel()`
     *
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [ViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * LiveData für das NEUE ProductItem
     */
    var note = MutableLiveData<String>()

    private val _product = MutableLiveData<Long>()
    val product: LiveData<Long>
        get() = _product

    var description = MutableLiveData<String>()

    private var _productItemValid = MutableLiveData<Boolean>()
    val productItemValid: LiveData<Boolean>
        get() = _productItemValid

    var existingProdId = 0L

    private fun fillProduct(id: Long, passedNote: String){
        uiScope.launch {
            val existingProd = getProductById(id)
            description.value = existingProd.user_Description
            note.value = passedNote
        }
    }

    private suspend fun getProductById(id:Long) : ProductItem{
        return withContext(Dispatchers.IO){
            val prod = prodDao.getProductById(id)
            prod
        }
    }

    /**
     * Executes when the CONFIRM button is clicked.
     */
    fun addProductClicked() {
        uiScope.launch {
            var prodId = existingProdId
            val newProd = ProductItem(existingProdId)
            newProd.user_Description = description.value.toString()
            if(existingProdId > 0){
                update(newProd)
            } else {
                prodId = insert(newProd)
            }
            _product.value = prodId
        }
    }

    fun validateProduct() {
        uiScope.launch {
            var valid = !description.value.isNullOrEmpty()
            _productItemValid.value = valid
        }
    }


    private suspend fun insert(newProd: ProductItem): Long {
        return withContext(Dispatchers.IO){
            prodDao.insert(newProd)
        }
    }
    private suspend fun update(product: ProductItem) {
        withContext(Dispatchers.IO){
            prodDao.update(product)
        }
    }



    /**
     * Called when the ViewModel is dismantled.
     * At this point, we want to cancel all coroutines;
     * otherwise we end up with processes that have nowhere to return to
     * using memory and resources.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun doneNavigatingToGroceryList() {
        _product.value = null
    }
}