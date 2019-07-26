package de.jl.groceriesmanager.database.inventory

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface InventoryDao {

    //TODO: Return LONG für ID
    @Query("SELECT COUNT(*) from inventory")
    fun getSize(): Int

    @Query("SELECT * FROM inventory ORDER BY inventory_id")
    fun getAllInventoryItems(): LiveData<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE inventory_id = :id")
    fun getInventoryItemById(id: Long): InventoryItem

    @Query("DELETE FROM inventory WHERE inventory_id = :id")
    fun remove(id: Long)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(item: InventoryItem): Long

    @Update(onConflict = OnConflictStrategy.FAIL)
    fun update(item: InventoryItem)

    @Query("SELECT * FROM inventory WHERE product_id = :prodId")
    fun getInventoryItemByProdId(prodId: Long): InventoryItem

    @Transaction
    fun upsert(item: InventoryItem): Long {
        var id: Long
        try {
            id = insert(item)
        } catch (e: SQLiteConstraintException) {
            update(item)
            id = item.inventory_id
        }
        return id
    }
}