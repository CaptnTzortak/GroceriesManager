package de.jl.groceriesmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import de.jl.groceriesmanager.database.groceryLists.GroceryListsDao
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProductsDao
import de.jl.groceriesmanager.database.inventory.Inventory
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.products.Product
import de.jl.groceriesmanager.database.products.ProductsDao

@Database(entities = [GroceryListsProducts::class, GroceryList::class, Product::class, Inventory::class], version = 2, exportSchema = false)
abstract class GroceriesManagerDB : RoomDatabase() {

    abstract val inventoryDao : InventoryDao
    abstract val productsDao : ProductsDao
    abstract val groceryListsDao : GroceryListsDao
    abstract val groceryListsProductsDao: GroceryListsProductsDao

    companion object {

        @Volatile
        private var INSTANCE: GroceriesManagerDB? = null

        fun getInstance(context: Context): GroceriesManagerDB {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GroceriesManagerDB::class.java,
                        "groceriesmanager_db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}