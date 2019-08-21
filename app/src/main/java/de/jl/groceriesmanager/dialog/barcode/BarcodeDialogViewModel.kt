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
    passedBarcode: Barcode?
) : AndroidViewModel(application) {


    private val invDao = GroceriesManagerDB.getInstance(application).inventoryDao
    private val prodDao = GroceriesManagerDB.getInstance(application).productsDao
    private val glDao = GroceriesManagerDB.getInstance(application).groceryListsDao
    private val barcodesDao = GroceriesManagerDB.getInstance(application).barcodesDao
    private val glProductsDao = GroceriesManagerDB.getInstance(application).groceryListsProductsDao

    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    private val _barcode = MutableLiveData<Barcode>()
    val barcode: LiveData<Barcode>
        get() = _barcode

    var existingProductNamesWithoutBarcode = prodDao.getNamesForAllExistingProductsWithoutBarcode()
    var existingGroceryListNames = glDao.getNamesForAllExistingGroceryLists()

    init {
        if (passedBarcode != null) {
            _barcode.value = passedBarcode
        }
    }

    fun addBarcodeAsProductAndGroceryListEntry(groceryListName: String) {
        uiScope.launch{
            //1. Barcode erstellen und in DB
            val bc = _barcode.value
            if (bc != null) {
                if (!barcodeAlreadyExists(bc.id)) {
                    insertBarcode(bc)
                }
                //2. Produkt erstellen, Barcode referenzieren und in DB
                val exProd = tryGetProductByBarcodeId(bc.id)
                val realProdId = if (exProd == null || exProd.id == 0L) {
                    //Es existiert noch kein Produkt
                    insertProduct(Product(0L, bc.id, bc.barcodeDescription))
                } else {
                    exProd.id
                }
                //3. GroceryList ermitteln und Eintrag erstellen
                val existingGl = getGroceryList(groceryListName)
                val glItemEntry = GroceryListsProducts(0L, realProdId, existingGl.id)
                insertGlItemEntry(glItemEntry)
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
                prod.barcode = _barcode.value!!
                prod.barcodeId = _barcode.value!!.id
                barcodesDao.insert(_barcode.value!!)
                prodDao.update(prod)
            }
        }
    }

    fun addBarcodeAsProductAndInventory(expiryDateString: String) {
        uiScope.launch {
            //1. Barcode erstellen und in DB
            val bc = _barcode.value
            if (bc != null) {

                if (!barcodeAlreadyExists(bc.id)) {
                    insertBarcode(bc)
                }
                //2. Produkt erstellen, Barcode referenzieren und in DB
                val exProd = tryGetProductByBarcodeId(bc.id)
                val realProdId = if (exProd == null || exProd.id == 0L) {
                    //Es existiert noch kein Produkt
                    insertProduct(Product(0L, bc.id, bc.barcodeDescription))
                } else {
                    exProd.id
                }
                //3. Inv Eintrag erstellen, Produkt und expiryDate referenzieren und in DB
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

    private suspend fun tryGetProductByBarcodeId(id: Long): Product? {
        return withContext(Dispatchers.IO) {
            prodDao.getProductByBarcodeId(id)
        }
    }

    private suspend fun barcodeAlreadyExists(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val bcCnt = barcodesDao.getCountById(id)
            bcCnt > 0L
        }
    }

    private suspend fun insertBarcode(bc: Barcode) {
        withContext(Dispatchers.IO) {
            barcodesDao.insert(bc)
        }
    }
}