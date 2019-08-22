package de.jl.groceriesmanager.database.products

import androidx.room.*

@Entity(tableName = "Products")
data class Product(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "barcodeId")
    var barcodeId: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "quantity")
    var quantity: String = "",

    @ColumnInfo(name ="brand")
    var brand: String = "",

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null
){

    fun getDescription() : String{
        var desc = name
        if(brand.isNotEmpty()){
            desc += " - $brand"
        }
        if(quantity.isNotEmpty()){
            desc += " - $quantity"
        }
        return desc
    }
}