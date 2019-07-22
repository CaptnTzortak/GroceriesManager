package de.jl.groceriesmanager.database.groceryList

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryListsDao {

    @Query("SELECT * FROM grocery_lists ORDER BY gl_id DESC")
    fun getAllGroceryLists(): LiveData<List<GroceryList>>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(groceryList: GroceryList): Long


    @Query("DELETE FROM grocery_lists WHERE gl_id = :id")
    fun remove(id: Long)


    ////TODO: Return LONG für ID
   //@Query("SELECT COUNT(*) from inventory")
   //fun getSize(): Int

   //@Query("SELECT * FROM inventory ORDER BY inventory_id DESC")
   //fun getAllInventoryItems(): LiveData<List<InventoryItem>>

   //@Query("SELECT * FROM inventory WHERE inventory_id = :id")
   //fun getInventoryItemById(id: Long): InventoryItem



   //@Insert(onConflict = OnConflictStrategy.FAIL)
   //fun insert(item: InventoryItem): Long

   //@Update
   //fun update(item: InventoryItem)

   //@Query("SELECT * FROM inventory WHERE product_id = :prodId")
   //fun getInventoryItemByProdId(prodId: Long): InventoryItem

}