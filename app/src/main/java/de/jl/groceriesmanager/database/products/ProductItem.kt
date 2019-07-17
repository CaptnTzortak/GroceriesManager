package de.jl.groceriesmanager.database.products

import android.widget.EditText
import androidx.databinding.InverseMethod
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="products")
data class ProductItem(

    //ID des Produktes
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    val product_id: Long = 0L,

    //Barcode
    @ColumnInfo(name="barcode_id")
    var barcode_id: Long = 0L,

    //User-Description
    @ColumnInfo(name="user_description")
    var user_description: String = "",

    //Expiry-Date
    @ColumnInfo(name="expiry_date")
    var expiry_date: String =""
){


    var userDescription: String
        get() = this.user_description
        set(value){
            this.user_description = value
        }
   // fun getId(): Long {
   //     return product_id
   // }
//
   // fun getBarcodeId(): Long{
   //     return barcode_id
   // }
//
   // fun getUserDescription(): String{
   //     return user_description
   // }
//
   // fun getExpiryDate(): String {
   //     return expiry_date
   // }
}