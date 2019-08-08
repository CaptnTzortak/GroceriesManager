package de.jl.groceriesmanager.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.jl.openfoodfacts.OpenFoodFactsApi
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.squareup.moshi.Types.newParameterizedType
import de.jl.openfoodfacts.OpenFoodFactsProperty


class ScannerViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    private val _valid = MutableLiveData<Boolean>()
    val valid: LiveData<Boolean>
        get() = _valid


    var barcode = MutableLiveData<String>()

    init {
        barcode.value = ""
        Log.i("ScannerViewModel", "init")
        getBarcodes()
    }

    private fun getBarcodes() {
        uiScope.launch {
            // Get the Deferred object for our Retrofit request
            OpenFoodFactsApi.retrofitService.getProperties().enqueue(
                object: Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                   //     _response.value = response.body()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                   //     _response.value = "Failure: " + t.message
                    }
                })

            var getProperties= OpenFoodFactsApi.retrofitService.getProperties()
           //    // Await the completion of our Retrofit request
           //    var listResult = getPropertiesDeferred.await()
           //    _response.value = "Success: ${listResult.size} Mars properties retrieved"
           //} catch (e: Exception) {
           //    _response.value = "Failure: ${e.message}"
           //}
        }
    }

    private suspend fun getResult() : String {
        return withContext(Dispatchers.IO){
            var result = ""
            try{
                result = URL("https://world.openfoodfacts.org/api/v0/product/${barcode.value}.json").readText()
            }catch (e: Exception){
                Log.e("ScanerViewModel", e.localizedMessage)
            }
            result
        }
    }

    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun validateBarcode() {
        uiScope.launch {
            var isValid = false
            if(barcode.value?.length == 13 ){
                isValid = barcode.value!!.toLong() >= 1000000000000L
            }
            _valid.value = isValid
        }
    }

    fun getData() {
        uiScope.launch {
            val result = getResult()
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<OpenFoodFactsProperty>(OpenFoodFactsProperty::class.java)
            val openFoodFactsProperty = adapter.fromJson(result)
            if(openFoodFactsProperty != null){
                if(openFoodFactsProperty.product != null){
                    _response.value = openFoodFactsProperty.product.generic_name
                } else {
                    _response.value = openFoodFactsProperty.code
                }
            }else{
                _response.value = "No response"
            }
        }
    }
}