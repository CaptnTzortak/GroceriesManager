package de.jl.groceriesmanager.database.inventory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="inventory")
data class InventoryItem(
    //ID
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    val inventory_id: Long = 0L,

    //ID des Produkts
    @ColumnInfo(name="product_id")
    var product_id: Long = 0L
)