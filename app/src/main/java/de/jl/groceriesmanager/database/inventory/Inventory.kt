package de.jl.groceriesmanager.database.inventory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "inventory")
data class Inventory(
    //ID
    @PrimaryKey(autoGenerate = true)
    val inventoryId: Long = 0L,

    //ID des referenzierten Items (Barcode oder eigene ID)
    @ColumnInfo(name = "item_id")
    val itemId: Long = 0L
)

//Verfallsdatum
//@ColumnInfo(name = "expiry_date")
//var originalDescription: LocalDate = LocalDate.now()
{

}