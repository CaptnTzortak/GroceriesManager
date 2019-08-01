package de.jl.groceriesmanager.database.products

import androidx.room.*

@Dao
interface BarcodesDao {
    @Delete
    fun delete(barcode: Barcode)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(barcode: Barcode): Long

    @Update(onConflict = OnConflictStrategy.FAIL)
    fun update(barcode: Barcode)

}