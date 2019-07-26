package de.jl.groceriesmanager.product.add.groceryList

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.database.products.ProductsDao

class AddProductGroceryListViewModelFactory(private val dataSource: ProductsDao, private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductGroceryListViewModel::class.java)) {
            return AddProductGroceryListViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}