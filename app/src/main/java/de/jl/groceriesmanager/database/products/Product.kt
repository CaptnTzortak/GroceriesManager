package de.jl.groceriesmanager.database.products

import androidx.room.*

@Entity(tableName = "Products",
    foreignKeys = [ForeignKey(entity = Barcode::class, parentColumns = ["id"], childColumns = ["barcodeId"])]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "barcodeId")
    var barcodeId: Long? = null,

    @ColumnInfo(name = "description")
    var description: String = "",

    @Ignore
    var barcode: Barcode = Barcode()
)