package de.jl.groceriesmanager.product.add

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jl.groceriesmanager.database.products.ProductsDao

class AddProductViewModelFactory(private val dataSource: ProductsDao, private val application: Application, private val prodId: Long) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
            return AddProductViewModel(dataSource, application, prodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}