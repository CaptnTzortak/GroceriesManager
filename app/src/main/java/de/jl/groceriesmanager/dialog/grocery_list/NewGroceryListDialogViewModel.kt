package de.jl.groceriesmanager.dialog.grocery_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryLists.GroceryList
import kotlinx.coroutines.*

class NewGroceryListDialogViewModel(application: Application, passedGlId: Long = 0L) : AndroidViewModel(application) {
    //job
    private var viewModelJob = Job()
    //UI-Scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val glsDao = GroceriesManagerDB.getInstance(application).groceryListsDao

    private val _addedGroceryList = MutableLiveData<Long>()
    val addedProduct : LiveData<Long>
    get() = _addedGroceryList

    private val _confirmGroceryList = MutableLiveData<Boolean>()
    val confirmProduct : LiveData<Boolean>
    get() = _confirmGroceryList

    var groceryListName = MutableLiveData<String>()

    var confirmGroceryListValid = MutableLiveData<Boolean>()

    var existingGlId: Long = 0L

    init {
        groceryListName.value = ""
        if(passedGlId > 0L){
            existingGlId = passedGlId
            setupEditGroceryList()
        }
        checkGroceryListNameValid()
    }

    private fun setupEditGroceryList() {
        uiScope.launch {
            val existingGl = getExistingGl(existingGlId)
            groceryListName.value = existingGl.name
        }
    }

    private suspend fun getExistingGl(passedGlId: Long): GroceryList {
        return withContext(Dispatchers.IO){
            glsDao.getGroceryListById(passedGlId)
        }
    }

    fun confirmGroceryListBtnClicked() {
        uiScope.launch {
            _confirmGroceryList.value = true
        }
    }


    fun insertOrUpdateGroceryList() {
        uiScope.launch {
            if(existingGlId > 0L){
               val groceryList = getExistingGl(existingGlId)
                groceryList.name = groceryListName.value.toString()
                update(groceryList)
                _addedGroceryList.value = groceryList.id
            } else{
                val groceryList = GroceryList(0L,groceryListName.value.toString())
                _addedGroceryList.value = insert(groceryList)
            }
        }
    }

    private suspend fun update(gl: GroceryList){
        withContext(Dispatchers.IO){
            glsDao.update(gl)
        }
    }

    private suspend fun insert(gl: GroceryList): Long{
        return withContext(Dispatchers.IO){
            glsDao.insert(gl)
        }
    }

    fun checkGroceryListNameValid() {
        uiScope.launch {
            confirmGroceryListValid.value = groceryListName.value?.length!! >= 3
        }
    }
}