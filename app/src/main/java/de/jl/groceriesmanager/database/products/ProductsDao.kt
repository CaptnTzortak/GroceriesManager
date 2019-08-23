package de.jl.groceriesmanager.database.products

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductsDao {

    @Query("SELECT * FROM Products WHERE barcodeId = :barcode")
    fun getProductByBarcode(barcode: Long): Product?

    @Query("SELECT * FROM Products WHERE id = :id")
    fun getProductById(id: Long): Product

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(product: Product): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(product: Product)

    @Query("SELECT DISTINCT * FROM Products")
    fun getNamesForAllExistingProducts(): LiveData<List<Product>>

    @Query("SELECT DISTINCT * FROM Products WHERE barcodeId = 0")
    fun getNamesForAllExistingProductsWithoutBarcode(): LiveData<List<Product>>

    @Query("SELECT * FROM Products WHERE name LIKE :name")
    fun getProductByName(name: String): Product?

}