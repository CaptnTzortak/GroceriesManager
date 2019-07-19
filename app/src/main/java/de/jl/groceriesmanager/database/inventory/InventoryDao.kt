package de.jl.groceriesmanager.database.inventory

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface InventoryDao {

    @Insert
    fun insert(item: InventoryItem)

    @Query("SELECT COUNT(*) from inventory")
    fun getSize(): Int

    @Query("SELECT * FROM inventory ORDER BY inventory_id DESC")
    fun getAllInventoryItems(): LiveData<List<InventoryItem>>


}