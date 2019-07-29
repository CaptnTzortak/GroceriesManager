package de.jl.groceriesmanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.grocery_list.GroceryListViewModel
import de.jl.groceriesmanager.grocery_lists.GroceryListsViewModel
import de.jl.groceriesmanager.inventory.InventoryViewModel
import de.jl.groceriesmanager.product.add.AddProductViewModel
import de.jl.groceriesmanager.product.add.groceryList.AddProductGroceryListViewModel
import de.jl.groceriesmanager.scanner.ScannerViewModel

class GroceriesManagerViewModelFactory(
    private val application: Application,
    private val prodDao: ProductsDao? = null,
    private val invDao: InventoryDao? = null,
    private val glDao: GroceryListsDao? = null,
    private val glItemMappingDao: GLItemMappingDao? = null,
    private val glId: Long = 0L,
    private val prodId: Long = 0L
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ScannerViewModel::class.java) -> return ScannerViewModel() as T
            modelClass.isAssignableFrom(AddProductViewModel::class.java) -> return prodDao?.let {
                AddProductViewModel(
                    application,
                    prodId,
                    it
                )
            } as T
            modelClass.isAssignableFrom(AddProductGroceryListViewModel::class.java) -> return prodDao?.let {
                AddProductGroceryListViewModel(application,
                    it
                )
            } as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> return invDao?.let {
                prodDao?.let { it1 ->
                    InventoryViewModel(
                        application,
                        it, it1
                    )
                }
            } as T
            modelClass.isAssignableFrom(GroceryListsViewModel::class.java) -> return glDao?.let {
                GroceryListsViewModel(application,
                    it
                )
            } as T
            modelClass.isAssignableFrom(GroceryListViewModel::class.java) -> return glItemMappingDao?.let {
                glDao?.let { it1 ->
                    prodDao?.let { it2 ->
                        GroceryListViewModel(application, glId,
                            it, it1, it2
                        )
                    }
                }
            } as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}