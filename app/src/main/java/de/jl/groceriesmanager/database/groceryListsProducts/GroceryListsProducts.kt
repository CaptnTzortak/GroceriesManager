package de.jl.groceriesmanager.database.groceryListsProducts

import androidx.room.*
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import de.jl.groceriesmanager.database.products.Product


@Entity(
    tableName = "GroceryListsProducts",
    foreignKeys = [ForeignKey(entity = Product::class, parentColumns = ["id"], childColumns = ["prodId"]),
        ForeignKey(entity = GroceryList::class, parentColumns = ["id"], childColumns = ["glId"])]
)
data class GroceryListsProducts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "prodId")
    var prodId: Long = 0L,

    @ColumnInfo(name = "glId")
    var glId: Long = 0L,

    @ColumnInfo(name = "note")
    var note: String = "",

    @ColumnInfo(name = "bought")
    var bought: Boolean = false,

    @Ignore
    var product: Product? = null,

    @Ignore
    var groceryList: GroceryList = GroceryList()
)