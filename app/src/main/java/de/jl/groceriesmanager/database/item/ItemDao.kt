package de.jl.groceriesmanager.database.item

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {

    @Insert
    fun insert(item: Item)

    @Update
    fun update(item: Item)

    @Query("SELECT * from item WHERE itemId = :key")
    fun get(key: Long): Item?

    @Query("DELETE FROM item")
    fun clear()

    @Query("SELECT * FROM item ORDER BY itemId DESC")
    fun getAllItems(): LiveData<List<Item>>
}