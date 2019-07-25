package de.jl.groceriesmanager.database.inventory

import androidx.lifecycle.LiveData
import androidx.room.*
import de.jl.groceriesmanager.database.gl_item_mapping.GLItemMapping
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.products.ProductItem

@Dao
interface GLItemMappingDao {

    @Query("SELECT * from gl_item_mapping where id = :glId ")
    fun getAllProductsInGL(glId: Long): LiveData<List<GLItemMapping>>

    @Query("SELECT * FROM grocery_lists where id = :glId")
    fun getGroceryListById(glId: Long): LiveData<GroceryList>

    @Query("SELECT * FROM PRODUCTS where product_id in (SELECT product_id FROM gl_item_mapping where id = :glId)")
    fun getProductsListByGroceryListId(glId: Long): LiveData<List<ProductItem>>


    //TODO: Return LONG für ID
    @Query("SELECT COUNT(*) from inventory")
    fun getSize(): Int

    @Query("SELECT * FROM inventory ORDER BY inventory_id DESC")
    fun getAllInventoryItems(): LiveData<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE inventory_id = :id")
    fun getInventoryItemById(id: Long): InventoryItem

    @Query("DELETE FROM inventory WHERE inventory_id = :id")
    fun remove(id: Long)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(item: InventoryItem): Long

    @Update
    fun update(item: InventoryItem)

    @Query("SELECT * FROM inventory WHERE product_id = :prodId")
    fun getInventoryItemByProdId(prodId: Long): InventoryItem

}