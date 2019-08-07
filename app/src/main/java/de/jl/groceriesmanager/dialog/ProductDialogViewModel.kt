package de.jl.groceriesmanager.dialog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.Product
import kotlinx.coroutines.*
import kotlin.math.exp

class ProductDialogViewModel(application: Application, passedProdId: Long = 0L, passedExpiryDateString: String = "") : AndroidViewModel(application) {
    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val prodsDao = GroceriesManagerDB.getInstance(application).productsDao

    private val _addedProduct = MutableLiveData<Long>()
    val addedProduct : LiveData<Long>
    get() = _addedProduct

    private val _confirmProduct = MutableLiveData<Boolean>()
    val confirmProduct : LiveData<Boolean>
    get() = _confirmProduct

    var productDescription = MutableLiveData<String>()
    var expiryDateString = MutableLiveData<String>()

    var existingProdId: Long = 0L
    var existingExpiryDateString: String = ""

    init {
        if(passedProdId > 0L){
            existingProdId = passedProdId
            existingExpiryDateString = passedExpiryDateString
            setupEditProduct()
        }
    }

    private fun setupEditProduct() {
        uiScope.launch {
            val existingProd = getExistingProd(existingProdId)
            if(existingProd != null){
                productDescription.value = existingProd.description
                expiryDateString.value = existingExpiryDateString
            }
        }
    }

    private suspend fun getExistingProd(passedProdId: Long): Product {
        return withContext(Dispatchers.IO){
            prodsDao.getProductById(passedProdId)
        }
    }

    fun confirmProductBtnClicked() {
        uiScope.launch {
            _confirmProduct.value = true
        }
    }


    fun insertOrUpdateProduct() {
        uiScope.launch {
            if(existingProdId > 0L){
               val product = getExistingProd(existingProdId)
                product.description = productDescription.value.toString()
                update(product)
                _addedProduct.value = product.id
            } else{
                val product = Product(0L,null, productDescription.value.toString())
                _addedProduct.value = insert(product)
            }
        }
    }

    private suspend fun update(prod: Product){
        withContext(Dispatchers.IO){
            prodsDao.update(prod)
        }
    }

    private suspend fun insert(prod: Product): Long{
        return withContext(Dispatchers.IO){
            prodsDao.insert(prod)
        }
    }
}