package de.jl.groceriesmanager.database.inventory

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface InventoryDao {

    @Query("select * from inventory where expiryDateString < strftime('%d.%m.%Y', date('now', +'3 day'))")
    fun getInventoryEntrysExpireNextThreeDays(): List<Inventory>

    @Query("SELECT prodId FROM Inventory WHERE id = :id")
    fun getProdIdByInvId(id: Long): Long

    @Query("SELECT * FROM Inventory")
    fun getAllInventorys(): LiveData<List<Inventory>>

    @Query("SELECT * FROM Inventory WHERE id = :id")
    fun getInventoryById(id: Long):Inventory

    @Query("SELECT * FROM Inventory WHERE prodId = :id")
    fun getInventoryByProdId(id: Long): Inventory

    @Query("DELETE FROM Inventory WHERE id = :id")
    fun deleteById(id: Long)

    @Delete
    fun delete(inventory: Inventory)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(inventory: Inventory): Long

    @Update(onConflict = OnConflictStrategy.FAIL)
    fun update(inventory: Inventory)
}