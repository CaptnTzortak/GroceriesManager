package de.jl.groceriesmanager.database.inventory

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GLItemMappingDao {

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