package de.jl.groceriesmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.jl.groceriesmanager.database.groceryList.GroceryList
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao

@Database(entities = [InventoryItem::class, ProductItem::class, GroceryList::class], version = 5, exportSchema = false)
abstract class GroceriesManagerDB : RoomDatabase() {

    abstract val inventoryDao : InventoryDao
    abstract val productsDao : ProductsDao
    abstract val groceryListsDao: GroceryListsDao

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
                        "groceries_manager_history_db"
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