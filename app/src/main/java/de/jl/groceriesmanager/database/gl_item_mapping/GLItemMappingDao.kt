package de.jl.groceriesmanager.database.inventory

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import de.jl.groceriesmanager.database.gl_item_mapping.GLItemMapping
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.products.ProductItem
import java.lang.Exception

@Dao
interface GLItemMappingDao {

    @Query("UPDATE gl_item_mapping SET done = :done where gl_item_mapping_id = :id")
    fun flipDoneForItem(id: Long, done: Boolean)

    @Query("DELETE FROM gl_item_mapping WHERE gl_item_mapping_id = :id")
    fun remove(id: Long)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(item: GLItemMapping): Long

    @Update
    fun update(item: GLItemMapping)

    @Transaction
    fun upsert(item: GLItemMapping): Long {
        var id = item.gl_item_mapping_id
        try {
            id = insert(item)
        } catch (e: SQLiteConstraintException) {
            update(item)
        }
        return id
    }


    @Query("SELECT * from gl_item_mapping where id = :glId ")
    fun getAllProductsInGL(glId: Long): LiveData<List<GLItemMapping>>

    // @Query("SELECT * FROM grocery_lists where id = :glId")
    // fun getGroceryListById(glId: Long): LiveData<GroceryList>
    @Query("SELECT * FROM gl_item_mapping WHERE id = :glId")
    fun getItemMappingByGroceryListId(glId: Long): LiveData<List<GLItemMapping>>

    @Query("SELECT * FROM PRODUCTS where product_id in (SELECT product_id FROM gl_item_mapping where id = :glId)")
    fun getProductsListByGroceryListId(glId: Long): LiveData<List<ProductItem>>


    //TODO: Return LONG für ID
    @Query("SELECT COUNT(*) from inventory")
    fun getSize(): Int

    @Query("SELECT * FROM inventory ORDER BY inventory_id DESC")
    fun getAllInventoryItems(): LiveData<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE inventory_id = :id")
    fun getInventoryItemById(id: Long): InventoryItem



    @Query("SELECT * FROM inventory WHERE product_id = :prodId")
    fun getInventoryItemByProdId(prodId: Long): InventoryItem

}