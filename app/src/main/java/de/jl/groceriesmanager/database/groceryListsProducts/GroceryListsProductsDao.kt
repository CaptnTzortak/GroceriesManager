package de.jl.groceriesmanager.database.groceryListsProducts

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryListsProductsDao {

    @Query("SELECT prodId FROM GroceryListsProducts WHERE glId = :glId")
    fun getAllProductIdsByGlId(glId: Long): List<Long>

    @Query("SELECT prodId FROM GroceryListsProducts WHERE id = :id")
    fun getAllProductIdsByGLProductsId(id: Long): List<Long>

    @Query("DELETE FROM GroceryListsProducts WHERE glId = :glId")
    fun deleteAllGroceryListsProductsByGlId(glId: Long)

    @Query("SELECT * FROM GroceryListsProducts WHERE glId = :glId ORDER BY bought")
    fun getAllGroceryListsProducts(glId: Long): LiveData<List<GroceryListsProducts>>

    @Query("SELECT * FROM GroceryListsProducts WHERE id = :id")
    fun getGroceryListsProductsById(id: Long): GroceryListsProducts

    @Query("DELETE FROM GroceryListsProducts WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM GroceryListsProducts WHERE glId = :glId AND prodId = :prodId")
    fun deleteProductsByGLIdAndProdId(glId:Long, prodId:Long)

    @Query("SELECT * FROM GroceryListsProducts WHERE prodId = :prodId")
    fun getGroceryListsProductsEntryById(prodId: Long): GroceryListsProducts?

    @Query("UPDATE GroceryListsProducts SET bought=:bought WHERE id = :id")
    fun setBoughtForGLPById(id: Long, bought: Boolean)

    @Query("UPDATE GroceryListsProducts SET bought=:bought WHERE glId = :glId")
    fun setBoughtForGroceryList(glId: Long, bought: Boolean)

    @Delete
    fun delete(groceryList: GroceryListsProducts)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(groceryList: GroceryListsProducts): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(groceryList: GroceryListsProducts)
}