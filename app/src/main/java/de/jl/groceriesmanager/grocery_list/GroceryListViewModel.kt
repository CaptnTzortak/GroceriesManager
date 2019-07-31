package de.jl.groceriesmanager.grocery_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts
import de.jl.groceriesmanager.database.inventory.Inventory
import kotlinx.coroutines.*

class GroceryListViewModel(
    application: Application, private val glId: Long
) : AndroidViewModel(application) {

    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val glpDao = GroceriesManagerDB.getInstance(application).groceryListsProductsDao
    private val invDao = GroceriesManagerDB.getInstance(application).inventoryDao
    private val glDao = GroceriesManagerDB.getInstance(application).groceryListsDao
    private val prodDao = GroceriesManagerDB.getInstance(application).productsDao


    //region LiveData
    /**
     * Live Data für das neue Produkt welches der Einkaufsliste hinzugefügt wird.
     * z.B aus dem addProductFragment/View (Navigation Args)
     */
    private val _newProductGroceryListItem = MutableLiveData<Pair<Long, String>>()
    val newProductGroceryListItem: LiveData<Pair<Long, String>>
        get() = _newProductGroceryListItem

    val groceryListProducts = glpDao.getAllGroceryListsProducts(glId)

    private val _filledGroceryListProducts = MutableLiveData<List<GroceryListsProducts>>()
    val filledGroceryListProducts: LiveData<List<GroceryListsProducts>>
        get() = _filledGroceryListProducts


    private val _reset = MutableLiveData<Long>()
    val reset: LiveData<Long>
        get() = _reset

    /**
     * LiveData für den Button "Add" im GroceryList-Screen. Dieser navigiert dann zum
     * AddGroceryListItem-Screen
     */
    private val _addProduct = MutableLiveData<Pair<Long, String>>()
    val addProduct: LiveData<Pair<Long, String>>
        get() = _addProduct


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
    fun doneReset() {
        _reset.value = null
    }

    fun resetClicked() {
        _reset.value = glId
    }

    /**
     * Wird aus dem Layout aufgerufen (Btn add)
     */
    fun addProductClicked() {
        _addProduct.value = Pair(0L, "")
    }

    /**
     * Resetter für das LiveData-Objekt  nach der Navigation zum AddProductGroceryLists-Screen
     */
    fun doneNavigatingToAddProductGL() {
        _addProduct.value = null
    }


    fun addProductToInventory(glProductsId: Long, expiryDateString: String) {
        uiScope.launch {
            val prodId = getProductIdByGLProductsId(glProductsId)
            val invEntry = Inventory(0L, prodId, expiryDateString)
            insertInventoryItem(invEntry)
        }
    }

    private suspend fun insertInventoryItem(item: Inventory){
        withContext(Dispatchers.IO){
            invDao.insert(item)
        }
    }

    private suspend fun getProductIdByGLProductsId(glProductsId: Long): Long {
        return withContext(Dispatchers.IO) {
            val ids = glpDao.getAllProductIdsByGLProductsId(glProductsId)
            ids.first()
        }
    }

    /**
     * Context-Menü "Delete" ruft diese Funktion auf.
     */
    fun deleteGroceryListsProducts(glProductsId: Long) {
        uiScope.launch {
            val productIds = getAllProdIdsByGLProductsId(glProductsId)
            removeGroceryListsProductsEntryById(glProductsId)
            removeProducts(productIds)
        }
    }

    private suspend fun getAllProdIdsByGLProductsId(id: Long): List<Long> {
        return withContext(Dispatchers.IO) {
            glpDao.getAllProductIdsByGLProductsId(id)
        }
    }

    private suspend fun removeProducts(ids: List<Long>) {
        withContext(Dispatchers.IO) {
            ids.iterator().forEach {
                prodDao.deleteById(it)
            }
        }
    }

    private fun setGLName(id: Long) {
        uiScope.launch {
            val existingGL = getGroceryListById(id)
            glName.value = existingGL.name
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
     * entfernt abh. der GLItemMapping-ID ein GLItemMapping-Objekt aus der GLItemMapping-Tabelle
     */
    private suspend fun removeGroceryListsProductsEntryById(id: Long) {
        withContext(Dispatchers.IO) {
            glpDao.deleteById(id)
        }
    }

    private suspend fun flipDoneForItem(item: GroceryListsProducts) {
        withContext(Dispatchers.IO) {
            glpDao.setBoughtForGLPById(item.id, !item.bought)
        }
    }

    fun flipDoneForGLItemMapping(item: GroceryListsProducts?) {
        uiScope.launch {
            if (item != null) {
                flipDoneForItem(item)
            }
        }
    }

    fun addNewProductItem() {
        uiScope.launch {
            val prodId = _newProductGroceryListItem.value?.first
            val note = _newProductGroceryListItem.value?.second
            if (prodId != null && note != null) {
                var entry = getExistingGroceryListsProductsEntry(prodId)
                entry.glId = glId
                entry.prodId = prodId
                entry.note = note
                if (entry.id > 0L) {
                    updateEntry(entry)
                } else {
                    insertEntry(entry)
                }
            }
        }
    }

    private suspend fun updateEntry(groceryListsProducts: GroceryListsProducts) {
        withContext(Dispatchers.IO) {
            glpDao.update(groceryListsProducts)
        }
    }

    private suspend fun insertEntry(groceryListsProducts: GroceryListsProducts): Long {
        return withContext(Dispatchers.IO) {
            glpDao.insert(groceryListsProducts)
        }
    }

    private suspend fun getExistingGroceryListsProductsEntry(prodId: Long): GroceryListsProducts {
        return withContext(Dispatchers.IO) {
            var glp = glpDao.getGroceryListsProductsEntryById(prodId)
            if (glp == null) {
                glp = GroceryListsProducts()
            }
            glp
        }
    }
    //endregion

    /**
     * Diese Funktion wird aufgerufen, wenn über den AddProduct-Screen
     * ein neues Produkt hinzugefügt wird und dieses über die Navigation-Arguments
     * dem InventoryFragment übergeben werden.
     */
    fun newProductInserted(productId: Long, note: String) {
        uiScope.launch {
            _newProductGroceryListItem.value = Pair(productId, note)
        }
    }

    fun fillGroceryListProducts() {
        uiScope.launch {
            fillGroceryListsProductsDetails()
        }
    }

    private suspend fun fillGroceryListsProductsDetails() {
        _filledGroceryListProducts.value = withContext(Dispatchers.IO) {
            groceryListProducts.value?.iterator()?.forEach {
                it.product = prodDao.getProductById(it.prodId)
                it.groceryList = glDao.getGroceryListById(it.glId)
            }
            groceryListProducts.value
        }
    }

    fun modifyProduct(item: GroceryListsProducts) {
        uiScope.launch {
            _addProduct.value = Pair(item.prodId, item.note)
        }
    }

    fun resetGLProducts(glId: Long) {
        uiScope.launch {
            resetBought(glId)
        }
    }

    private suspend fun resetBought(glId: Long) {
        withContext(Dispatchers.IO) {
            glpDao.setBoughtForGroceryList(glId, false)
        }
    }
}