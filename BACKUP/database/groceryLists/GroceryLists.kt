package de.jl.groceriesmanager.database.groceryLists

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "grocery_lists")
data class GroceryLists(
    //ID
    @PrimaryKey(autoGenerate = true)
    val groceryListId: Long = 0L,

    //ID des referenzierten Items (Barcode oder eigene ID)
    @ColumnInfo(name = "Description")
    val description: String = ""

) {

}