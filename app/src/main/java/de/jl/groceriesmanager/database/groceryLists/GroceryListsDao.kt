package de.jl.groceriesmanager.database.groceryLists

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryListsDao {

    @Query("SELECT * FROM GroceryLists WHERE name LIKE :name")
    fun getGroceryListByName(name: String): GroceryList

    @Query("SELECT name FROM GroceryLists")
    fun getNamesForAllExistingGroceryLists(): LiveData<List<String>>

    @Query("SELECT * FROM GroceryLists ORDER BY name")
    fun getAllGroceryLists(): LiveData<List<GroceryList>>

    @Query("SELECT * FROM GroceryLists WHERE id = :id")
    fun getGroceryListById(id: Long): GroceryList

    @Query("DELETE FROM GroceryLists WHERE id = :id")
    fun deleteById(id: Long)

    @Delete
    fun delete(groceryList: GroceryList)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(groceryList: GroceryList): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(groceryList: GroceryList)
}