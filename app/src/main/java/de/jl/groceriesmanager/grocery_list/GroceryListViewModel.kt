package de.jl.groceriesmanager.grocery_list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.gl_item_mapping.GLItemMapping
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import kotlinx.coroutines.*

class GroceryListViewModel(
    application: Application, private val glId: Long, private val glItemMappingDao: GLItemMappingDao, private val glDao: GroceryListsDao, private val prodDao: ProductsDao) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //region LiveData
    /**
     * Live Data für das neue Produkt welches der Einkaufsliste hinzugefügt wird.
     * z.B aus dem addProductFragment/View (Navigation Args)
     */
    private val _newProductGroceryListItem = MutableLiveData<Long>()
    val newProductGroceryListItem: LiveData<Long>
        get() = _newProductGroceryListItem


    /**
     * LiveData für den Button "Add" im GroceryList-Screen. Dieser navigiert dann zum
     * AddGroceryListItem-Screen
     */
    private val _addProduct = MutableLiveData<Long>()
    val addProduct: LiveData<Long>
        get() = _addProduct


    /**
     * LiveData welche die Datenbank-Elemente für die selektierte GroceryList ermittelt.
     * Der Adapter übernimmt die Darstellung
     */
    val glItemMappingList = glItemMappingDao.getItemMappingByGroceryListId(glId)

    /**
     * LiveData mit two-way-binding zum setzen und ermitteln des GroceryList-Namens
     */
    val glName = MutableLiveData<String>()
    //endregion

    init {
        if (glId > 0) {
            setGLName(glId)
        }
    }

    //region helpers
    /**
     * Wird aus dem Layout aufgerufen (Btn add)
     */
    fun addProductClicked() {
        _addProduct.value = 0L
    }

    /**
     * Resetter für das LiveData-Objekt  nach der Navigation zum AddProductGroceryLists-Screen
     */
    fun doneNavigatingToAddProductGL() {
        _addProduct.value = null
    }

    /**
     * Context-Menü "Delete" ruft diese Funktion auf.
     */
    fun deleteGroceryListEntry(id: Long) {
        uiScope.launch {
            removeGLItemMapping(id)
        }
    }

    private fun setGLName(id: Long) {
        uiScope.launch {
            val existingGL = getGroceryListById(id)
            glName.value = existingGL.description
        }
    }

    //endregion

    //region DB-Threads
    /**
     * returniert abh. der GroceryList-ID ein GroceryList-Objekt aus der GroceryLists-Tabelle
     */
    private suspend fun getGroceryListById(id: Long): GroceryList {
        return withContext(Dispatchers.IO) {
            val gl = glDao.getGroceryListById(id)
            gl
        }
    }

    /**
     * returniert abh. der Product-ID ein ProductItem-Objekt aus der Products-Tabelle
     */
    private suspend fun getProductById(prodId: Long): ProductItem? {
        return withContext(Dispatchers.IO) {
            val product = prodDao.getProductById(prodId)
            product
        }
    }

    /**
     * entfernt abh. der GLItemMapping-ID ein GLItemMapping-Objekt aus der GLItemMapping-Tabelle
     */
    private suspend fun removeGLItemMapping(id: Long) {
        withContext(Dispatchers.IO) {
            glItemMappingDao.remove(id)
        }
    }

    private suspend fun flipDoneForItem(item: GLItemMapping) {
        withContext(Dispatchers.IO) {
            glItemMappingDao.flipDoneForItem(item.gl_item_mapping_id, item.done)
        }
    }

    fun flipDoneForGLItemMapping(item: GLItemMapping?) {
        uiScope.launch {
            if (item != null) {
                flipDoneForItem(item)
            }
        }
    }

    fun addNewProductItem() {
        uiScope.launch {
            val prodId = _newProductGroceryListItem.value
            if (prodId != null) {
                var itemMapping = GLItemMapping()
                itemMapping.groceryList = getGroceryListById(glId)
                itemMapping.product = getProductById(prodId)
                upsert(itemMapping)
            }
        }
    }
    //endregion

    /**
     * DB-Thread/Abschnitt zum inserieren oder updaten
     * eines InventoryItems.
     * NOTWENDIGKEIT: InventoryItem mit gesetztem Produkt.
     * ID nicht notwendig da ID-Eintrag von ROOM erstellt wird.
     */
    private suspend fun upsert(item: GLItemMapping) {
        withContext(Dispatchers.IO) {
            try {
                glItemMappingDao.upsert(item)
            } catch (e: Exception) {
                Log.d("InventoryViewModel", e.localizedMessage)
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
            _newProductGroceryListItem.value = productId
        }
    }
}