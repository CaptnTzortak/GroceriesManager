package de.jl.groceriesmanager.product.add.groceryList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.Product
import kotlinx.coroutines.*

class AddProductGroceryListViewModel(application: Application, private val prodId: Long, private val passedNote: String, private val passedGlId: Long) : AndroidViewModel(application) {
    /** Coroutine variables */

    /**
     * viewModelJob allows us to cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob = Job()

    private val prodDao = GroceriesManagerDB.getInstance(application).productsDao

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

    var existingProdId = 0L
    var glId = 0L

    init {
        if(prodId > 0) {
            fillProduct(prodId, passedNote)
            existingProdId = prodId
            glId = passedGlId
        }
    }

    private fun fillProduct(id: Long, passedNote: String) {
        uiScope.launch {
            val existingProd = getProductById(id)
            description.value = existingProd.description
            note.value = passedNote
        }
    }

    private suspend fun getProductById(id: Long): Product {
        return withContext(Dispatchers.IO) {
            prodDao.getProductById(id)
        }
    }

    /**
     * Executes when the CONFIRM button is clicked.
     */
    fun addProductClicked() {
        uiScope.launch {
            var prodId = existingProdId
            val newProd = Product(existingProdId)
            newProd.description = description.value.toString()
            if (existingProdId > 0) {
                update(newProd)
            } else {
                prodId = insert(newProd)
            }
            _product.value = prodId
        }
    }

    private suspend fun insert(newProd: Product): Long {
        return withContext(Dispatchers.IO) {
            prodDao.insert(newProd)
        }
    }

    private suspend fun update(product: Product) {
        withContext(Dispatchers.IO) {
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