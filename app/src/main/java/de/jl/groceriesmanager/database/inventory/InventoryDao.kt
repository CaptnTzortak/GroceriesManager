package de.jl.groceriesmanager.database.inventory

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.jl.groceriesmanager.database.item.Item

@Dao
interface InventoryDao {

    @Insert
    fun insert(item: Item)

    @Query("SELECT COUNT(*) from inventory")
    fun getSize(): Int


}