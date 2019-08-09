package de.jl.groceriesmanager.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import de.jl.groceriesmanager.database.products.Barcode
import de.jl.openfoodfacts.OpenFoodFactsApi
import de.jl.openfoodfacts.OpenFoodFactsProperty
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream


class ScannerViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _response = MutableLiveData<Barcode>()
    val response: LiveData<Barcode>
        get() = _response

    private val _valid = MutableLiveData<Boolean>()
    val valid: LiveData<Boolean>
        get() = _valid


    private val _scannedBarcode = MutableLiveData<String>()
    val scannedBarcode: LiveData<String>
        get() = _scannedBarcode


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
                object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        //     _response.value = response.body()
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        //     _response.value = "Failure: " + t.message
                    }
                })

            var getProperties = OpenFoodFactsApi.retrofitService.getProperties()
            //    // Await the completion of our Retrofit request
            //    var listResult = getPropertiesDeferred.await()
            //    _response.value = "Success: ${listResult.size} Mars properties retrieved"
            //} catch (e: Exception) {
            //    _response.value = "Failure: ${e.message}"
            //}
        }
    }

    private suspend fun getResult(): String {
        return withContext(Dispatchers.IO) {
            var result = ""
            try {
                result = URL("https://world.openfoodfacts.org/api/v0/product/${barcode.value}.json").readText()
            } catch (e: Exception) {
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
            if (barcode.value?.length == 13) {
                isValid = barcode.value!!.toLong() >= 1000000000000L
            }
            _valid.value = isValid
        }
    }

    fun getData() {
        uiScope.launch {
            _response.value = getBarcodeByOpenFoodFactsJSON()
        }
    }

    private suspend fun getBarcodeByOpenFoodFactsJSON(): Barcode? {
        val oFFProperty = withContext(Dispatchers.IO) {
            val result = getResult()
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<OpenFoodFactsProperty>(OpenFoodFactsProperty::class.java)
            adapter.fromJson(result)
        }
        return parseOFFProductToBarcode(oFFProperty)
    }

    private suspend fun parseOFFProductToBarcode(oFFProperty: OpenFoodFactsProperty?): Barcode? {
        try {
            if (oFFProperty != null && oFFProperty.status == 1) {
                //JSON-Success
                if (oFFProperty.product.product_name.isNotEmpty()) {
                    val barcodeId = oFFProperty.product.id.toLong()
                    val productName = oFFProperty.product.product_name
                    val brands = oFFProperty.product.brands
                    val quantity = oFFProperty.product.quantity
                    val barcodeDescription = "$productName - $brands - $quantity"
                    val barcodeImgUrl = oFFProperty.product.image_url
                    val image = getBitmapByUrl(barcodeImgUrl)
                    val commonName = oFFProperty.product.generic_name
                    val categories = oFFProperty.product.categories

                    return Barcode(
                        barcodeId,
                        barcodeDescription,
                        productName,
                        brands,
                        quantity,
                        image,
                        barcodeImgUrl,
                        commonName,
                        categories
                    )
                }
            }
            return null
        } catch (e: Exception) {
            Log.e("ScannerViewModel", e.localizedMessage)
            return null
        }
    }

    private suspend fun getBitmapByUrl(passedUrl: String) : ByteArray? {
        return withContext(Dispatchers.IO){
            val url = URL(passedUrl)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 90, stream)
            val image = stream.toByteArray()
            image
        }

    }

    fun scanFailed() {
        uiScope.launch {
            _scannedBarcode.value = null
        }
    }

    fun setScannedBarcode(displayValue: String?) {
        uiScope.launch {
            if(displayValue != null && displayValue.isNotEmpty()){
                _scannedBarcode.value = displayValue
            }
        }
    }

    fun setBarcode(it: String?) {
        uiScope.launch {
            if(it != null){
                barcode.value = it
            }
        }

    }
}
