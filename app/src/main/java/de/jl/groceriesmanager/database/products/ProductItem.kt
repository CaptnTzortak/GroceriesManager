package de.jl.groceriesmanager.database.products

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "products")
data class ProductItem(

    //ID des Produktes
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "product_id")
    val product_id: Long = 0L,

  // @Embedded
  // var barcode: Barcode? = null
  // //Barcode
    @ColumnInfo(name = "barcode_id")
    var barcode_id: Long = 0L,

    //User-Description
    @ColumnInfo(name = "user_description")
    var user_Description: String = "",

    //Expiry-Date
    @ColumnInfo(name = "expiry_date_string")
    var expiry_date_string: String = ""
)