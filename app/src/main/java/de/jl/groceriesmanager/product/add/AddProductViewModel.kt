package de.jl.groceriesmanager.product.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*

class AddProductViewModel(val database: ProductsDao, application: Application) : AndroidViewModel(application) {
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
    private val _product = MutableLiveData<ProductItem?>()
    val product: LiveData<ProductItem?>
        get() = _product

    var description = MutableLiveData<String>()
    var expiryDate = MutableLiveData<String>()

    private var _productItemValid = MutableLiveData<Boolean>()
    val productItemValid: LiveData<Boolean>
        get() = _productItemValid

    /**
     * Executes when the CONFIRM button is clicked.
     */
    fun onConfirmItem() {
        uiScope.launch {
            val newProd = ProductItem()
            newProd.user_Description = description.value.toString()
            newProd.expiry_date = expiryDate.value.toString()
            insert(newProd)
            _product.value = getLatestProduct()
        }
    }

    fun validateProduct() {
        uiScope.launch {
            var valid = !description.value.isNullOrEmpty()
            _productItemValid.value = valid
        }
    }

    private suspend fun getLatestProduct() : ProductItem? {
        return withContext(Dispatchers.IO){
            var prod = database.getLatest()
            prod
        }
    }

    private suspend fun insert(product: ProductItem){
        withContext(Dispatchers.IO){
            database.insert(product)
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
}