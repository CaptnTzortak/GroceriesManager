package de.jl.groceriesmanager.database.gl_item_mapping

import androidx.room.*
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.products.ProductItem

@Entity(tableName="gl_item_mapping")
data class GLItemMapping (
    //ID
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="gl_item_mapping_id")
    val gl_item_mapping_id: Long = 0L,

    @ColumnInfo(name="note")
    val note: String = "",
    @Embedded
    var groceryList: GroceryList? = null,

    @Embedded
    var product: ProductItem? = null
)