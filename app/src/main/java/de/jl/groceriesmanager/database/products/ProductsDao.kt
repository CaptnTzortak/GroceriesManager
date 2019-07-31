package de.jl.groceriesmanager.database.products

import androidx.room.*

@Dao
interface ProductsDao {

    @Query("SELECT * FROM Products WHERE id = :id")
    fun getProductById(id: Long): Product

    @Query("DELETE FROM Products WHERE id = :id")
    fun deleteById(id: Long)

    @Delete
    fun delete(product: Product)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(product: Product): Long

    @Update(onConflict = OnConflictStrategy.FAIL)
    fun update(product: Product)

}