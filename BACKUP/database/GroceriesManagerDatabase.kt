package de.jl.groceriesmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.jl.groceriesmanager.database.groceryListItemMapping.GroceryListItemMapping
import de.jl.groceriesmanager.database.groceryListItemMapping.GroceryListItemMappingDao
import de.jl.groceriesmanager.database.groceryLists.GroceryListDao
import de.jl.groceriesmanager.database.groceryLists.GroceryLists
import de.jl.groceriesmanager.database.inventory.Inventory
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.item.Item
import de.jl.groceriesmanager.database.item.ItemDao


@Database(entities = [Item::class, Inventory::class, GroceryLists::class, GroceryListItemMapping::class], version = 1, exportSchema = false)
abstract class GroceriesManagerDatabase : RoomDatabase() {

    abstract val itemDao : ItemDao
    abstract val inventoryDao : InventoryDao
    abstract val groceryListsDao : GroceryListDao
    abstract val GroceryListItemMapping : GroceryListItemMappingDao

    companion object {

        @Volatile
        private var INSTANCE: GroceriesManagerDatabase? = null

        fun getInstance(context: Context): GroceriesManagerDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GroceriesManagerDatabase::class.java,
                        "groceries_manager_history_database"
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