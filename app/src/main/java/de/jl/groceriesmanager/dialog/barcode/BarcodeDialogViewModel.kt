package de.jl.groceriesmanager.dialog.barcode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.products.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class BarcodeDialogViewModel(
    application: Application,
    passedBarcode: Barcode?
) : AndroidViewModel(application) {
    fun referenceProductBtnClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    private val _barcode = MutableLiveData<Barcode>()
    val barcode: LiveData<Barcode>
        get() = _barcode


    init {
        if (passedBarcode != null) {
            _barcode.value = passedBarcode
        }
    }

}