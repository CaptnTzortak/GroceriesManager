package de.jl.groceriesmanager.inventory

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.inventory.Inventory
import kotlinx.coroutines.*

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val invDao = GroceriesManagerDB.getInstance(application).inventoryDao
    private val prodDao = GroceriesManagerDB.getInstance(application).productsDao

    /**
     * LiveData für die Inventory-Einträge
     */
    val inventories = invDao.getAllInventorys()

    private val _inventoriesWithProduct = MutableLiveData<List<Inventory>>()
    val inventoriesWithProduct: LiveData<List<Inventory>>
        get() = _inventoriesWithProduct


    /**
     * Live Data für das neue Produkt welches einem Inventar hinzugefügt wird.
     * z.B aus dem addProductFragment/View (Navigation Args)
     */
    private val _newProductInventoryItem = MutableLiveData<Pair<Long, Pair<Long, String>>>()
    val newProductInventoryItem: LiveData<Pair<Long, Pair<Long, String>>>
        get() = _newProductInventoryItem

    private val _removeInventoryItem = MutableLiveData<Long>()
    val removeInventoryItem: LiveData<Long>
        get() = _removeInventoryItem

    private val _navigateToAddProduct = MutableLiveData<Pair<Long, Pair<Long,String>>>()
    val navigateToAddProduct: LiveData<Pair<Long, Pair<Long,String>>>
        get() = _navigateToAddProduct

    /**
     * Wird vom Button AddInventoryiItem
     * beim OnClick ausgeführt
     */
    fun onAddInventoyItem() {
        uiScope.launch {
            _navigateToAddProduct.value = Pair(0L, Pair(0L, ""))
        }
    }

    /**
     * Resetter für den Observer NavigateToAddProduct
     */
    fun doneNavigatingToAddProduct() {
        _navigateToAddProduct.value = null
    }

    /**
     * Diese Funktion wird aufgerufen, wenn der Nutzer auf
     * einen RecylcerView-Eintrag klickt. Hier bekommen wir die InventoryId.
     * Es muss die Product_ID ermittelt werden
     * */
    fun modifyProduct(inventoryId: Long) {
        uiScope.launch {
            val invItem = getInventoryItemById(inventoryId)
            if (invItem?.prodId!! > 0L) {
                _navigateToAddProduct.value = Pair(inventoryId, Pair(invItem.prodId, invItem.expiryDateString))
            }
        }
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
            val invId = _newProductInventoryItem.value?.first ?: 0L
            val prodId = _newProductInventoryItem.value?.second?.first
            val expiryDateString = _newProductInventoryItem.value?.second?.second

            if (prodId != null) {
                val invItem = Inventory(invId, prodId, expiryDateString.toString())
                if (invItem.id > 0L) {
                    update(invItem)
                } else {
                    insert(invItem)
                }
            }
        }
    }


    /**
     * Diese Funktion wird aufgerufen, wenn über den AddProduct-Screen
     * ein neues Produkt hinzugefügt wird und dieses über die Navigation-Arguments
     * dem InventoryFragment übergeben werden.
     */
    fun newProductInserted(invIdWithProdIdAndExpiryDate: Pair<Long, Pair<Long, String>>) {
        uiScope.launch {
            _newProductInventoryItem.value = invIdWithProdIdAndExpiryDate
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
     * DB-Thread/Abschnitt welcher ein InventoryItem anhand
     * der InventoryID ermittelt
     */
    private suspend fun getInventoryItemById(id: Long): Inventory? {
        return withContext(Dispatchers.IO) {
            invDao.getInventoryById(id)
        }
    }

    /**
     * DB-Thread/Abschnitt zum inserieren oder updaten
     * eines InventoryItems.
     * NOTWENDIGKEIT: InventoryItem mit gesetztem Produkt.
     * ID nicht notwendig da ID-Eintrag von ROOM erstellt wird.
     */
    private suspend fun insert(item: Inventory): Long {
        return withContext(Dispatchers.IO) {
            invDao.insert(item)
        }
    }


    /**
     * DB-Thread/Abschnitt zum inserieren oder updaten
     * eines InventoryItems.
     * NOTWENDIGKEIT: InventoryItem mit gesetztem Produkt.
     * ID nicht notwendig da ID-Eintrag von ROOM erstellt wird.
     */
    private suspend fun update(item: Inventory) {
        withContext(Dispatchers.IO) {
            invDao.update(item)
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
                invDao.deleteById(inventoryId)
            } catch (e: Exception) {
                Log.d("InventoryViewModel", e.localizedMessage)
            }
        }
    }

    fun fillInventoryProducts() {
        uiScope.launch {
            fillInventoryWithProducts()
        }
    }

    private suspend fun fillInventoryWithProducts() {
        _inventoriesWithProduct.value = withContext(Dispatchers.IO) {
            inventories.value?.iterator()?.forEach {
                it.product = prodDao.getProductById(it.prodId)
            }
            inventories.value
        }
    }

    fun doneAddNewProductItem() {
        uiScope.launch {
            _newProductInventoryItem.value = null
        }
    }
}

