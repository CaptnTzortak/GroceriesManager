package de.jl.groceriesmanager.database.item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class Item(
    //ID ist Barcode falls ermittelt oder eigene ID
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0L,

    //Name / Text der das Item beschreibt
    @ColumnInfo(name = "description")
    val description: String = "",

    //Original-Beschreibung die von der API ermittelt wird
    @ColumnInfo(name = "original_description")
    var originalDescription: String = "",

    //Anzahl bzw Menge welche von der API ermittelt wird
    @ColumnInfo(name = "original_quantity")
    var originalQuantity: String = "",

    //Marke welche von der API ermittelt wird
    @ColumnInfo(name = "original_brand")
    var originalBrand: String = ""
) {

}