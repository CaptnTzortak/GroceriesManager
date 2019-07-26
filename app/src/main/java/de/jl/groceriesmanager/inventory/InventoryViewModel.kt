package de.jl.groceriesmanager.inventory

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*
import java.lang.Exception

class InventoryViewModel(val database: InventoryDao, val prodDataBase: ProductsDao, application: Application) :
    AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()

    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val inventoryItems = database.getAllInventoryItems()

    /**
     * Live Data für das neue Produkt welches einem Inventar hinzugefügt wird.
     * z.B aus dem addProductFragment/View (Navigation Args)
     */
    private val _newProductInventoryItem = MutableLiveData<Long>()
    val newProductInventoryItem: LiveData<Long>
        get() = _newProductInventoryItem

    private val _removeInventoryItem = MutableLiveData<Long>()
    val removeInventoryItem: LiveData<Long>
        get() = _removeInventoryItem

    private val _navigateToAddProduct = MutableLiveData<Long>()
    val navigateToAddProduct: LiveData<Long>
        get() = _navigateToAddProduct


    /**
     * Wird vom Button AddInventoryiItem
     * beim OnClick ausgeführt
     */
    fun onAddInventoyItem() {
        uiScope.launch {
            _navigateToAddProduct.value = 0L
        }
    }

    /**
     * Resetter für den Observer NavigateToAddProduct
     */
    fun doneNavigatingToAddProduct() {
        _navigateToAddProduct.value = null
    }

    /**
     * Wird vom Observer aufgerufen falls
     * der Nutzer auf das Kontextmenü "Delete" klickt
     */
    fun removeInventoryItem() {
        uiScope.launch {
            val inventoryId = _removeInventoryItem.value
            if (inventoryId != null) {
                remove(inventoryId)
            }
        }
    }

    /**
     * addNewProductItem wird über den Observer auf newProductInventoryItem
     * im InventoryFragment gestartet. Dieser führt dazu, das das Produkt anhand der ID
     * ermittelt wird und danach die Referenz des Inventorys auf das Produkt gesetzt wird.
     * Das setzen der Referenz geschieht im DB-Thread/Abschnitt insert()
     */
    fun addNewProductItem() {
        uiScope.launch {
            val prodId = _newProductInventoryItem.value
            if (prodId != null) {
                val product = getProductById(prodId)
                val invItem = getNewOrExistingInventoryItem(prodId)
                invItem.product = product
                upsert(invItem)
            }
        }
    }


    /**
     * Diese Funktion wird aufgerufen, wenn über den AddProduct-Screen
     * ein neues Produkt hinzugefügt wird und dieses über die Navigation-Arguments
     * dem InventoryFragment übergeben werden.
     */
    fun newProductInserted(productId: Long) {
        uiScope.launch {
            _newProductInventoryItem.value = productId
        }
    }


    /**
     * Diese Funktion wird aufgerufen, wenn der Nutzer im
     * RecyclerView-Kontextmenü auf "Löschen" klickt
     */
    fun deleteInventoryItem(inventoryId: Long) {
        uiScope.launch {
            _removeInventoryItem.value = inventoryId
        }
    }

    /**
     * Diese Funktion wird aufgerufen, wenn der Nutzer auf
     * einen RecylcerView-Eintrag klickt. Hier bekommen wir die InventoryId.
     * Es muss die Product_ID ermittelt werden
     * */
    fun modifyProduct(inventoryId: Long) {
        uiScope.launch {
            val invItem = getInventoryItemById(inventoryId)
            if (invItem?.product != null) {
                _navigateToAddProduct.value = invItem.product!!.product_id
            }
        }
    }

    /**
     * DB-Thread/Abschnitt welcher ein InventoryItem anhand
     * der InventoryID ermittelt
     */
    private suspend fun getInventoryItemById(id: Long): InventoryItem? {
        return withContext(Dispatchers.IO) {
            var invItem = InventoryItem()
            try{
                invItem = database.getInventoryItemById(id)
            } catch (e: Exception){
                Log.d("InventoryViewModel", e.localizedMessage)
            }
            invItem
        }
    }

    /**
     * DB-Thread/Abschnitt zum inserieren oder updaten
     * eines InventoryItems.
     * NOTWENDIGKEIT: InventoryItem mit gesetztem Produkt.
     * ID nicht notwendig da ID-Eintrag von ROOM erstellt wird.
     */
    private suspend fun upsert(item: InventoryItem) {
        withContext(Dispatchers.IO) {
            try{
                database.upsert(item)
            } catch (e: Exception){
                Log.d("InventoryViewModel", e.localizedMessage)
            }

        }
    }



    /**
     * DB-Thread/Abschnitt zum ermitteln des Produkt-Objektes
     * anhand der Produkt-ID
     */
    private suspend fun getProductById(prodId: Long): ProductItem {
        return withContext(Dispatchers.IO) {
            var prod = ProductItem()
            try {
                prod = prodDataBase.getProductById(prodId)
            }catch (e: Exception){
                Log.d("InventoryViewModel", e.localizedMessage)
            }
            prod
        }
    }

    /**
     * DB-Thread/Abschnitt zum entfernen eines
     * Inventory-Items. Dh dem Eintrag aus der Inventory-Tabelle.
     * Das Referenzierte Produkt existiert noch in der Products-Tabelle.
     */
    private suspend fun remove(inventoryId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val item = database.getInventoryItemById(inventoryId)
                database.remove(inventoryId)
                val prodId = item.product?.product_id ?: return@withContext
                prodDataBase.remove(prodId)
            }catch (e: Exception){
                Log.d("InventoryViewModel", e.localizedMessage)
            }
        }
    }

    /**
     * DB-Thread/Abschnitt zum ermitteln eines
     * neuen oder schon bestehenden InventoryItem abh.
     * der ProduktId
     */
    private suspend fun getNewOrExistingInventoryItem(prodId: Long): InventoryItem {
        return withContext(Dispatchers.IO) {
            var invItem = InventoryItem()
            try {
                if (prodId > 0) {
                    invItem = database.getInventoryItemByProdId(prodId)
                    if (invItem == null) {
                        invItem = InventoryItem()
                    }
                }
            }catch(e: Exception){
                Log.d("InventoryViewModel",e.localizedMessage)
            }
            invItem
        }
    }
}
