package de.jl.groceriesmanager.database

import android.content.Context
import de.jl.groceriesmanager.database.inventory.Inventory

class GroceriesManagerRepository(context: Context) {

    val inventorysWithExpiryDateCloserOne : List<Inventory>

    init{
        val db = GroceriesManagerDB.getInstance(context)
        inventorysWithExpiryDateCloserOne = db.inventoryDao.getInventoryEntrysExpireNextThreeDays()
        inventorysWithExpiryDateCloserOne.iterator().forEach {
            if (it.prodId > 0L){
                it.product = db.productsDao.getProductById(it.prodId)
            }
        }
    }
}