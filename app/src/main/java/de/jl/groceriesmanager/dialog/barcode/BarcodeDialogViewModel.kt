package de.jl.groceriesmanager.dialog.barcode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts
import de.jl.groceriesmanager.database.inventory.Inventory
import de.jl.groceriesmanager.database.products.Barcode
import de.jl.groceriesmanager.database.products.Product
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


    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product>
        get() = _product

    var existingProductNamesWithoutBarcode = prodDao.getNamesForAllExistingProductsWithoutBarcode()
    var existingGroceryListNames = glDao.getNamesForAllExistingGroceryLists()

    init {
        if (passedProduct != null) {
            _product.value = passedProduct
        }
    }

    fun addBarcodeAsProductAndGroceryListEntry(groceryListName: String) {
        uiScope.launch{
            //1. Barcode erstellen und in DB
            val bc = _product.value
            if (bc != null) {
                //TODO
                //if (!barcodeAlreadyExists(bc.id)) {
                //    insertBarcode(bc)
                //}
                ////2. Produkt erstellen, Barcode referenzieren und in DB
                //val exProd = tryGetProductByBarcodeId(bc.id)
                //val realProdId = if (exProd == null || exProd.id == 0L) {
                //    //Es existiert noch kein Produkt
                //    insertProduct(Product(0L, bc.id, bc.barcodeDescription))
                //} else {
                //    exProd.id
                //}
                ////3. GroceryList ermitteln und Eintrag erstellen
                //val existingGl = getGroceryList(groceryListName)
                //val glItemEntry = GroceryListsProducts(0L, realProdId, existingGl.id)
                //insertGlItemEntry(glItemEntry)
            }
        }
    }

    private suspend fun getGroceryList(groceryListName: String): GroceryList {
        return withContext(Dispatchers.IO){
            glDao.getGroceryListByName(groceryListName)
        }
    }

    fun referenceBarcodeWithProduct(prodName: String) {
        uiScope.launch {
            refBarcodeWithProd(prodName)
        }

    }

    private suspend fun refBarcodeWithProd(prodName: String) {
        withContext(Dispatchers.IO) {
            val prod = prodDao.getProductByDescription(prodName)
            if (prod != null) {
                //TODO:
               //prod.barcode = _product.value!!
               //prod.barcodeId = _product.value!!.id
               //barcodesDao.insert(_product.value!!)
               //prodDao.update(prod)
            }
        }
    }

    fun addBarcodeAsProductAndInventory(expiryDateString: String) {
        uiScope.launch {
            //1. Barcode erstellen und in DB
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

    private suspend fun insertGlItemEntry(glEntry: GroceryListsProducts){
        withContext(Dispatchers.IO){
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
        return withContext(Dispatchers.IO){
            prodDao.getProductByBarcode(barcode)
        }
    }

    private suspend fun barcodeAlreadyExists(id: Long): Boolean {
        //TODO
        //return withContext(Dispatchers.IO) {
        //    val bcCnt = barcodesDao.getCountById(id)
        //    bcCnt > 0L
        //}
        return false
    }

    private suspend fun insertBarcode(bc: Barcode) {
        withContext(Dispatchers.IO) {
            //barcodesDao.insert(bc)
        }
    }
}