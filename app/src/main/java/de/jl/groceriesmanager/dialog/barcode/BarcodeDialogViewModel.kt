package de.jl.groceriesmanager.dialog.barcode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts
import de.jl.groceriesmanager.database.inventory.Inventory
import de.jl.groceriesmanager.database.products.Product
import de.jl.tools.parseProductDescriptionToProdName
import kotlinx.coroutines.*

class BarcodeDialogViewModel(
    application: Application,
    passedProduct: Product?
) : AndroidViewModel(application) {


    private val invDao = GroceriesManagerDB.getInstance(application).inventoryDao
    private val prodDao = GroceriesManagerDB.getInstance(application).productsDao
    private val glDao = GroceriesManagerDB.getInstance(application).groceryListsDao
    private val glProductsDao = GroceriesManagerDB.getInstance(application).groceryListsProductsDao

    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    val productNameString = MutableLiveData<String>()
    val quantityString = MutableLiveData<String>()
    val brandString = MutableLiveData<String>()

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product>
        get() = _product

    var existingProductNamesWithoutBarcode = prodDao.getNamesForAllExistingProductsWithoutBarcode()
    var existingGroceryListNames = glDao.getNamesForAllExistingGroceryLists()

    init {
        if (passedProduct != null) {
            _product.value = passedProduct
            fillUiByProduct()
        }
    }

    private fun fillUiByProduct() {
        if(_product.value != null){
            productNameString.value = _product.value!!.name
            quantityString.value = _product.value!!.quantity
            brandString.value = _product.value!!.brand
        }
    }

    private fun fillProductByUI(){
        if(_product.value != null){
            _product.value!!.name = productNameString.value?.toString() ?: _product.value!!.name
            _product.value!!.quantity = quantityString.value?.toString() ?: _product.value!!.quantity
            _product.value!!.brand = brandString.value?.toString() ?: _product.value!!.brand
        }
    }


    fun addBarcodeAsProductAndGroceryListEntry(groceryListName: String) {
        uiScope.launch {
            //1. Barcode erstellen und in DB
            fillProductByUI()
            val product = _product.value
            if (product != null) {
                //1. Existiert ein Produkt mit diesem Barcode?
                val existingProduct = tryGetExistingProductByBarcode(product.barcodeId)

                //Falls noch kein produkt in der Datenbank existiert muss ein neues hinzugefügt werden
                val realProdId = existingProduct?.id ?: insertProduct(product)

                //3. GroceryList ermitteln und Eintrag erstellen
                val existingGl = getGroceryList(groceryListName)
                val glItemEntry = GroceryListsProducts(0L, realProdId, existingGl.id)
                insertGlItemEntry(glItemEntry)
            }
        }
    }

    private suspend fun getGroceryList(groceryListName: String): GroceryList {
        return withContext(Dispatchers.IO) {
            glDao.getGroceryListByName(groceryListName)
        }
    }

    fun referenceBarcodeWithProduct(prodName: String) {
        uiScope.launch {
            fillProductByUI()
            refBarcodeWithProd(prodName)
        }

    }

    private suspend fun refBarcodeWithProd(prodName: String) {
        withContext(Dispatchers.IO) {
            val prod = prodDao.getProductByName(parseProductDescriptionToProdName(prodName))
            val barcodeProd = _product.value
            if (prod != null && barcodeProd != null) {
                prod.barcodeId = barcodeProd.barcodeId
                prod.quantity = barcodeProd.quantity
                prod.name = barcodeProd.name
                prod.brand = barcodeProd.brand
                prodDao.update(prod)
            }
        }
    }

    fun addBarcodeAsProductAndInventory(expiryDateString: String) {
        uiScope.launch {
            //1. Barcode erstellen und in DB
            fillProductByUI()
            val product = _product.value
            if (product != null) {
                //1. Existiert ein Produkt mit diesem Barcode?
                val existingProduct = tryGetExistingProductByBarcode(product.barcodeId)

                //Falls noch kein produkt in der Datenbank existiert muss ein neues hinzugefügt werden
                val realProdId = existingProduct?.id ?: insertProduct(product)

                //Neuer Inventar-Eintrag erstellen. Produkt und ExpiryDate referenzieren.
                val invEntry = Inventory(0L, realProdId, expiryDateString)
                insertInventoryEntry(invEntry)
            }
        }
    }

    private suspend fun insertGlItemEntry(glEntry: GroceryListsProducts) {
        withContext(Dispatchers.IO) {
            glProductsDao.insert(glEntry)
        }
    }

    private suspend fun insertInventoryEntry(invEntry: Inventory) {
        withContext(Dispatchers.IO) {
            invDao.insert(invEntry)
        }
    }

    private suspend fun insertProduct(prod: Product): Long {
        return withContext(Dispatchers.IO) {
            val id = prodDao.insert(prod)
            id
        }
    }

    private suspend fun tryGetExistingProductByBarcode(barcode: Long): Product? {
        return withContext(Dispatchers.IO) {
            prodDao.getProductByBarcode(barcode)
        }
    }
}