package de.jl.groceriesmanager.database.groceryList

import androidx.room.*

@Entity(tableName="grocery_lists")
data class GroceryList(
    //ID
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    val groceryList_id: Long = 0L,

    @ColumnInfo(name="description")
    val description: String = ""

)