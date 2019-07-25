package de.jl.groceriesmanager.database.groceryList

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryListsDao {

    @Query("SELECT * FROM grocery_lists ORDER BY id")
    fun getAllGroceryLists(): LiveData<List<GroceryList>>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(groceryList: GroceryList): Long

    @Query("DELETE FROM grocery_lists WHERE id = :id")
    fun remove(id: Long)
}