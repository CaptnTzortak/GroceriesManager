package de.jl.groceriesmanager.database.products

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductsDao {

    @Query("SELECT * FROM Products WHERE barcodeId = :barcode")
    fun getProductByBarcode(barcode: Long): Product?

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

    @Query("SELECT DISTINCT * FROM Products")
    fun getNamesForAllExistingProducts(): LiveData<List<Product>>

    @Query("SELECT DISTINCT * FROM Products WHERE barcodeId = 0")
    fun getNamesForAllExistingProductsWithoutBarcode(): LiveData<List<Product>>

    @Query("SELECT * FROM Products WHERE name LIKE :name")
    fun getProductByName(name: String): Product?

}