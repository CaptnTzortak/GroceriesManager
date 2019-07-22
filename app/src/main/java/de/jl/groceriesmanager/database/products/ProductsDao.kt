package de.jl.groceriesmanager.database.products

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import de.jl.groceriesmanager.database.inventory.InventoryItem


@Dao
interface ProductsDao {

    @Insert
    fun insert(product: ProductItem): Long

    @Query("SELECT COUNT(*) from products")
    fun getSize(): Int

    @Query("SELECT * FROM products ORDER BY product_id DESC")
    fun getAllProductItems(): LiveData<List<ProductItem>>

    @Query("Select * FROM products ORDER BY product_id DESC LIMIT 1")
    fun getLatest(): ProductItem

    @Query("SELECT * FROM products WHERE product_id = :prodId")
    fun getProductById(prodId: Long): ProductItem

    @Update
    fun update(product: ProductItem)

    @Query("DELETE FROM products WHERE product_id = :id")
    fun remove(id: Long)
}