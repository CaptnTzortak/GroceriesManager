package de.jl.groceriesmanager.database.products

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Barcodes")
data class Barcode(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "barcodeDescription")
    var barcodeDescription: String = "",

    @ColumnInfo(name = "productName")
    var productName: String = "",

    @ColumnInfo(name = "brands")
    var brands: String = "",

    @ColumnInfo(name = "quantity")
    var quantity: String = "",

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null,

    @ColumnInfo(name = "barcodeImgUrl")
    var barcodeImgUrl: String = "",

    @ColumnInfo(name ="commonName")
    var commonName: String = "",

    @ColumnInfo(name ="categories")
    var categories: String = ""
)