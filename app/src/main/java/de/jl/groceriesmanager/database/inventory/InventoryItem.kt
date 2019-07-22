package de.jl.groceriesmanager.database.inventory

import androidx.room.*
import de.jl.groceriesmanager.database.products.ProductItem

@Entity(tableName="inventory")
data class InventoryItem(
    //ID
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="inventory_id")
    val inventory_id: Long = 0L,

    ////ID des Produkts
    //@ColumnInfo(name="product_id")
    //var product_id: Long = 0L,

    @Embedded
    var product: ProductItem? = null
){
}