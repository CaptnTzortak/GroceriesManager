package de.jl.groceriesmanager.dialog.product

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.Product
import kotlinx.coroutines.*

class ProductDialogViewModel(
    application: Application,
    passedProdId: Long,
    passedExpiryDateString: String?,
    passedNote: String?,
    passedQuantity: Int?
) : AndroidViewModel(application) {
    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val prodsDao = GroceriesManagerDB.getInstance(application).productsDao

    private val _addedProduct = MutableLiveData<Long>()
    val addedProduct: LiveData<Long>
        get() = _addedProduct

    private val _confirmProduct = MutableLiveData<Boolean>()
    val confirmProduct: LiveData<Boolean>
        get() = _confirmProduct

    var productDescription = MutableLiveData<String>()
    var expiryDateString = MutableLiveData<String>()
    var note = MutableLiveData<String>()
    var quantityString = MutableLiveData<String>()

    var confirmProductValid = MutableLiveData<Boolean>()

    private var existingProdId: Long = 0L
    private var existingExpiryDateString: String? = null
    private var existingNote: String? = null
    private var existingQuantity: Int? = null

    init {
        productDescription.value = ""
        if (passedProdId > 0L) {
            existingProdId = passedProdId
            //Produkt ist vorhanden. Haben wir nun ein Verfallsdatum? -> Inventar
            //oder haben wir eine Notiz -> GL-Eintrag
            if (passedExpiryDateString != null && passedNote != null) {
                Log.d("ProductDialogViewModel", "ExpiryDate and Note filled")
            } else if (passedExpiryDateString != null) {
                //ExpiryDate anzeigen
                existingExpiryDateString = passedExpiryDateString
            } else if (passedNote != null && passedQuantity != null) {
                //Note anzeigen
                existingQuantity = passedQuantity
                existingNote = passedNote
            }
            setupEditProduct()
        }
        if (passedExpiryDateString != null && passedNote != null) {
            Log.d("ProductDialogViewModel", "ExpiryDate and Note filled")
        } else if (passedExpiryDateString != null) {
            expiryDateString.value = ""
        } else if (passedNote != null && passedQuantity != null) {
            note.value = ""
            val x = 1
            quantityString.value = x.toString()
        }
        checkProductValid()
    }

    private fun setupEditProduct() {
        uiScope.launch {
            val existingProd = getExistingProd(existingProdId)
            if (existingProd != null) {
                productDescription.value = existingProd.description
                expiryDateString.value = existingExpiryDateString
                note.value = existingNote
                quantityString.value = existingQuantity.toString()
            }
        }
    }

    private suspend fun getExistingProd(passedProdId: Long): Product {
        return withContext(Dispatchers.IO) {
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
            if (existingProdId > 0L) {
                val product = getExistingProd(existingProdId)
                product.description = productDescription.value.toString()
                update(product)
                _addedProduct.value = product.id
            } else {
                val product = Product(0L, null, productDescription.value.toString())
                _addedProduct.value = insert(product)
            }
        }
    }

    private suspend fun update(prod: Product) {
        withContext(Dispatchers.IO) {
            prodsDao.update(prod)
        }
    }

    private suspend fun insert(prod: Product): Long {
        return withContext(Dispatchers.IO) {
            prodsDao.insert(prod)
        }
    }

    fun checkProductValid() {
        uiScope.launch {
            confirmProductValid.value =
                productDescription.value?.length!! >= 3 && (!expiryDateString.value.isNullOrEmpty() || checkGLValidation())
        }
    }

    private fun checkGLValidation(): Boolean {
        return !note.value.isNullOrEmpty() && Integer.parseInt(quantityString.value!!) > 0
    }
}