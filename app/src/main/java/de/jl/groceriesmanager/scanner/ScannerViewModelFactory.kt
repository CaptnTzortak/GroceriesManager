package de.jl.groceriesmanager.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ScannerViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>) : T {
        if(modelClass.isAssignableFrom(ScannerViewModel::class.java)){
            return ScannerViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}