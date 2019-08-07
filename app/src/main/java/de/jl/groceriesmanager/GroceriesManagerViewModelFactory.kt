package de.jl.groceriesmanager

import android.app.Application
import android.content.ClipDescription
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.dialog.NewGroceryListDialogViewModel
import de.jl.groceriesmanager.dialog.ProductDialogViewModel
import de.jl.groceriesmanager.grocery_list.GroceryListViewModel
import de.jl.groceriesmanager.grocery_lists.GroceryListsViewModel
import de.jl.groceriesmanager.inventory.InventoryViewModel
import de.jl.groceriesmanager.product.add.AddProductViewModel
import de.jl.groceriesmanager.product.add.groceryList.AddProductGroceryListViewModel
import de.jl.groceriesmanager.scanner.ScannerViewModel

class GroceriesManagerViewModelFactory(
    private val application: Application,
    private val prodId: Long = 0L,
    private val expiryDateString: String? = null,
    private val glId: Long = 0L,
    private val passedNote: String? = null,
    private val passedQuantity: Int? = null

) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ScannerViewModel::class.java) -> return ScannerViewModel() as T
            modelClass.isAssignableFrom(AddProductViewModel::class.java) -> return AddProductViewModel(application, prodId, expiryDateString) as T
            modelClass.isAssignableFrom(AddProductGroceryListViewModel::class.java) -> return AddProductGroceryListViewModel(application, prodId, passedNote, glId) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> return InventoryViewModel(application) as T
            modelClass.isAssignableFrom(GroceryListsViewModel::class.java) -> return GroceryListsViewModel(application) as T
            modelClass.isAssignableFrom(GroceryListViewModel::class.java) -> return GroceryListViewModel(application, glId) as T
            modelClass.isAssignableFrom(ProductDialogViewModel::class.java) -> return ProductDialogViewModel(application, prodId, expiryDateString, passedNote, passedQuantity) as T
            modelClass.isAssignableFrom(NewGroceryListDialogViewModel::class.java) -> return NewGroceryListDialogViewModel(application, glId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}