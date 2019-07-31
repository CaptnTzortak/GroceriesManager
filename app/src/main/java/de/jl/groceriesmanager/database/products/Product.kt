package de.jl.groceriesmanager.database.products

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "barcodeId")
    var barcodeId: Long = 0L,

    @ColumnInfo(name = "description")
    var description: String = ""
)