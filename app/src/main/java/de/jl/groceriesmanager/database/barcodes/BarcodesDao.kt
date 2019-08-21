package de.jl.groceriesmanager.database.products

import androidx.room.*

@Dao
interface BarcodesDao {

    @Query("SELECT COUNT(*) FROM Barcodes WHERE id = :id")
    fun getCountById(id : Long): Long

    @Delete
    fun delete(barcode: Barcode)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(barcode: Barcode): Long

    @Update(onConflict = OnConflictStrategy.FAIL)
    fun update(barcode: Barcode)

}