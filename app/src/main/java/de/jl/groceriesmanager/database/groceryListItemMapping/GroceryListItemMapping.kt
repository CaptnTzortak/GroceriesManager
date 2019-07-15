package de.jl.groceriesmanager.database.groceryListItemMapping

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_list_item_mapping")
data class GroceryListItemMapping(
    //ID
    @PrimaryKey(autoGenerate = true)
    val mappingId: Long = 0L,

    @ColumnInfo(name = "grocery_list_id")
    val grorceryListId: Long = 0L,

    @ColumnInfo(name = "item_id")
    val itemId: Long = 0L,

    @ColumnInfo(name = "note")
    val note: String = ""
) {

}