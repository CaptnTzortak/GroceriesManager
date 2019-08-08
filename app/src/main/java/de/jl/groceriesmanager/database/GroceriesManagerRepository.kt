package de.jl.groceriesmanager.database

import android.content.Context
import androidx.lifecycle.LiveData
import de.jl.groceriesmanager.database.inventory.Inventory

class GroceriesManagerRepository(context: Context) {


    private val inventorysWithExpiryDateCloserOne : LiveData<List<Inventory>>

    init{
        val db = GroceriesManagerDB.getInstance(context)
        inventorysWithExpiryDateCloserOne = db.inventoryDao.getAllInventorys()
    }

    fun getAllInventoryEntrys() : LiveData<List<Inventory>>{
        return inventorysWithExpiryDateCloserOne
    }
}