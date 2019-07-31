package de.jl.groceriesmanager.product.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.Product
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*

class AddProductViewModel(application: Application, prodId : Long, expiryDate: String) : AndroidViewModel(application) {
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
    private val _productIdWithExpiryDate = MutableLiveData<Pair<Long,String>>()
    val productIdWithExpiryDate: LiveData<Pair<Long,String>>
        get() = _productIdWithExpiryDate

    var description = MutableLiveData<String>()
    var expiryDateString = MutableLiveData<String>()

    private var _productItemValid = MutableLiveData<Boolean>()
    val productItemValid: LiveData<Boolean>
        get() = _productItemValid

    var existingProdId = 0L

    init {
        if(prodId > 0) {
            fillProduct(prodId, expiryDate)
            existingProdId = prodId
        }
    }

    private fun fillProduct(id: Long, expiryDate: String){
        uiScope.launch {
            val existingProd = getProductById(id)
           description.value = existingProd?.description
           expiryDateString.value = expiryDate
        }
    }

    private suspend fun getProductById(id:Long) : Product?{
        return withContext(Dispatchers.IO){
            prodDao.getProductById(id)
        }
    }

    /**
     * Executes when the CONFIRM button is clicked.
     */
    fun onConfirmItem() {
        uiScope.launch {
            var prodId = existingProdId
            val newProd = Product(existingProdId)
            newProd.description = description.value.toString()
            val expiryDate = expiryDateString.value.toString()
            if(prodId != null && prodId > 0L){
                update(newProd)
            } else {
                prodId = insert(newProd)
            }
            _productIdWithExpiryDate.value = Pair(prodId, expiryDate)
        }
    }

    fun validateProduct() {
        uiScope.launch {
            var valid = !description.value.isNullOrEmpty()
            _productItemValid.value = valid
        }
    }


    private suspend fun insert(newProd: Product): Long{
        return withContext(Dispatchers.IO){
            val id = prodDao.insert(newProd)
            id
        }
    }

    private suspend fun update(prod: Product) {
        withContext(Dispatchers.IO){
            prodDao.update(prod)
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

    fun doneConfirmItem() {
        uiScope.launch {
            _productIdWithExpiryDate.value = null
        }
    }
}