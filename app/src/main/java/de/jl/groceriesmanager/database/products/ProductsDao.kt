package de.jl.groceriesmanager.database.products

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ProductsDao {

    @Insert
    fun insert(product: ProductItem)

    @Query("SELECT COUNT(*) from products")
    fun getSize(): Int

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductItems(): LiveData<List<ProductItem>>

    @Query("Select * FROM products ORDER BY id DESC LIMIT 1")
    fun getLatest(): ProductItem
}