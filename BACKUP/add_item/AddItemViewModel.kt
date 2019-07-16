package de.jl.groceriesmanager.add_item

import android.util.Log
import androidx.databinding.InverseMethod
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.jl.groceriesmanager.database.item.Item
import de.jl.groceriesmanager.database.item.ItemDao
import kotlinx.coroutines.*
import java.sql.Date

class AddItemViewModel(passedItem: Item?, val database: ItemDao) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }



    //START----UI-Controls
    private val _itemDescription = MutableLiveData<String>()
    val itemDescription: LiveData<String>
        get() = _itemDescription

    private val _id = MutableLiveData<Long>()
    val id: LiveData<Long>
        get() = _id

    //TODO: String-Anzeige des Wertes mit DataBinding und dem wirklichen Date wert
    private val _expiryDateString = MutableLiveData<String>()
    val expiryDateString: LiveData<String>
        get() = _expiryDateString

    private val _originalDescription = MutableLiveData<String>()
    val originalDescription: LiveData<String>
        get() = _originalDescription

    private val _quantity = MutableLiveData<String>()
    val quantity: LiveData<String>
        get() = _quantity

    private val _brand = MutableLiveData<String>()
    val brand: LiveData<String>
        get() = _brand
    //END------UI-Controls

    //START----Navigation-Controls
    private val _itemValid = MutableLiveData<Boolean>()
    val itemValid: LiveData<Boolean>
        get() = _itemValid

    private val _openExpiryDatePickerDialog = MutableLiveData<Boolean>()
    val openExpiryDatePickerDialog: LiveData<Boolean>
        get() = _openExpiryDatePickerDialog

    private val _navigateToInventory = MutableLiveData<Boolean>()
    val navigateToInventory: LiveData<Boolean>
        get() = _navigateToInventory
    //END------Navigation-Controls

    private lateinit var _newItem: Item
    val newItem: Item
            get() = _newItem








    //START----INIT
    init {
        try {
            Log.i("GroceryListViewModel", "init")
            _newItem = passedItem ?: Item()
            Log.d("GroceryListViewModel", newItem.itemId.toString() + " und " + newItem.description)

            _itemDescription.value = newItem.description
            _originalDescription.value = newItem.originalDescription
            _brand.value = newItem.originalBrand
            _quantity.value = newItem.originalQuantity
        } catch (e: Exception) {
            Log.d("AddItemViewModel", e.localizedMessage)
        }
    }
    //END------INIT

    //START----UI-Calls
    fun onSelectExpiryDate(){
        uiScope.launch {
            _openExpiryDatePickerDialog.value = true
        }
    }

    fun onConfirmItem(){
        uiScope.launch {
            _navigateToInventory.value = true
        }
    }
    //RESET----UI-Calls
    fun doneNavigatingToInventory() {
        _navigateToInventory.value = false
    }

    fun doneExpiryDatePickerDialog(){
        _openExpiryDatePickerDialog.value = false
    }

    fun validateItem(){
        if(_newItem == null) {
            Log.d("AddItemViewModel", "Error.... _newItem is Null")
        } else {
            _itemValid.value = (_newItem.itemId >= 0 && _newItem.description.isNotEmpty())
        }
    }

    fun itemDescriptionChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _newItem.description = s.toString()
    }


    fun onDescriptionTextChanged(){
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val description = ""
            }
        }
    }
    //END------UI-Calls

}