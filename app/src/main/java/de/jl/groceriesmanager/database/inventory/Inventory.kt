package de.jl.groceriesmanager.database.inventory

import androidx.room.*
import de.jl.groceriesmanager.database.products.Product

@Entity(tableName = "Inventory",
        foreignKeys = [ForeignKey(entity = Product::class, parentColumns = ["id"], childColumns = ["prodId"])])
data class Inventory(
   @PrimaryKey(autoGenerate = true)
   @ColumnInfo(name = "id")
   var id: Long = 0L,

   @ColumnInfo(name="prodId")
   var prodId: Long = 0L,

   @ColumnInfo(name = "expiryDateString")
   var expiryDateString: String = "",

   @Ignore
   var product: Product = Product()
)