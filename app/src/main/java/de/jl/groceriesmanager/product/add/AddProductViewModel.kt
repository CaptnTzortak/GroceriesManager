package de.jl.groceriesmanager.product.add

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

    private var _product = MutableLiveData<ProductItem?>()
    val product: LiveData<ProductItem?>
        get() = _product

    val confirmButtonVisible = Transformations.map(_product){
        validateProduct(_product)
    }

    /**
     * Variable that tells the Fragment to navigate to a specific [SleepQualityFragment]
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToInventory = MutableLiveData<ProductItem>()

    /**
     * If this is non-null, immediately navigate to [SleepQualityFragment] and call [doneNavigating]
     */
    val navigateToInventory: LiveData<ProductItem>
        get() = _navigateToInventory


    init{
        initializeProduct()
    }

    private fun initializeProduct() {
        _product.value = ProductItem()
    }


    /**
     * Executes when the CONFIRM button is clicked.
     */
    fun onConfirmItem() {
        uiScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val newProduct = product.value ?: return@launch
            insert(newProduct)
            // Set state to navigate to the SleepQualityFragment.
            _navigateToInventory.value = getProduct()
        }
    }

    /**
     *  Handling the case of the stopped app or forgotten recording,
     *  the start and end times will be the same.j
     *
     *  If the start time and end time are not the same, then we do not have an unfinished
     *  recording.
     */
    private suspend fun getProduct(): ProductItem? {
        return withContext(Dispatchers.IO) {
            var product = database.getLatest().value
            product
        }
    }

    private suspend fun insert(product: ProductItem) {
        withContext(Dispatchers.IO) {
            database.insert(product)
        }
    }

    fun productItemChanged(){
        validateProduct(_product)
    }

    private fun validateProduct(product: MutableLiveData<ProductItem?>): Boolean {
        if(product.value == null)
            return false

        if(!product.value?.user_description.isNullOrEmpty())
            return true

        return false
    }

    fun doneNavigatingToInventory(){
        _navigateToInventory.value = null
    }
}