package de.jl.groceriesmanager.database.groceryLists

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryListsDao {

    @Query("SELECT * FROM GroceryLists")
    fun getAllGroceryLists(): LiveData<List<GroceryList>>

    @Query("SELECT * FROM GroceryLists WHERE id = :id")
    fun getGroceryListById(id: Long): GroceryList

    @Query("DELETE FROM GroceryLists WHERE id = :id")
    fun deleteById(id: Long)

    @Delete
    fun delete(groceryList: GroceryList)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(groceryList: GroceryList): Long

    @Update(onConflict = OnConflictStrategy.FAIL)
    fun update(groceryList: GroceryList)
}