package de.jl.groceriesmanager.database.products

import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductItem(

    //ID des Produktes
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val product_id: Long = 0L,

    //Barcode
    @ColumnInfo(name = "barcode_id")
    var barcode_id: Long = 0L,

    //User-Description
    @ColumnInfo(name = "user_description")
    var user_Description: String = "",

    //Expiry-Date
    @ColumnInfo(name = "expiry_date")
    var expiry_date: String = ""

)