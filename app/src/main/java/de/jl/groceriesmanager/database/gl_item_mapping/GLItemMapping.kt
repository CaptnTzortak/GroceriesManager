package de.jl.groceriesmanager.database.inventory

import androidx.room.*
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.products.ProductItem

@Entity(tableName="gl_item_mapping", primaryKeys = ["gl_id", "product_id"])
data class GL_Item_Mapping(
    //ID
    @ColumnInfo(name="gl_id")
    val groceryList_id: Long = 0L,

    @ColumnInfo(name="product_id")
    val product_id: Long = 0L,

    @ColumnInfo(name="note")
    val note: String = "",

    @Embedded
    var product: ProductItem? = null,

    @Embedded
    var groceryList: GroceryList? = null
)