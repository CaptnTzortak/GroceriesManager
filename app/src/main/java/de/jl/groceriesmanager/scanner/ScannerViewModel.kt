package de.jl.groceriesmanager.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import de.jl.groceriesmanager.database.products.Barcode
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
    }

    private suspend fun getResult(): String {
        return withContext(Dispatchers.IO) {
            var result = ""
            try {
                result = URL("https://world-de.openfoodfacts.org/api/v0/product/${barcode.value}.json").readText()
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
            if (barcode.value?.length == 8 || barcode.value?.length == 13) {
                isValid = true
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
                if (!oFFProperty.product.product_name.isNullOrEmpty()) {
                    val barcodeId = oFFProperty.product.id.toLong()
                    val productName = oFFProperty.product.product_name
                    var brands = oFFProperty.product.brands
                    var quantity = oFFProperty.product.quantity
                    val barcodeDescription = "$productName - $brands - $quantity"
                    var barcodeImgUrl = oFFProperty.product.image_url
                    var image = barcodeImgUrl?.let { getBitmapByUrl(it) }
                    var commonName = oFFProperty.product.generic_name
                    if(brands.isNullOrEmpty()){
                        brands = ""
                    }
                    if(quantity.isNullOrEmpty()){
                        quantity = ""
                    }
                    if(barcodeImgUrl.isNullOrEmpty()){
                        barcodeImgUrl = ""
                    }
                    if(commonName.isNullOrEmpty()){
                        commonName = ""
                    }
                    return Barcode(
                        barcodeId,
                        barcodeDescription,
                        productName,
                        brands,
                        quantity,
                        image,
                        barcodeImgUrl,
                        commonName
                    )
                }
            }
            return null
        } catch (e: Exception) {
            Log.e("ScannerViewModel", e.localizedMessage)
            return null
        }
    }

    private suspend fun getBitmapByUrl(passedUrl: String): ByteArray? {
        return withContext(Dispatchers.IO) {
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
            if (displayValue != null && displayValue.isNotEmpty()) {
                _scannedBarcode.value = displayValue
            }
        }
    }

    fun setBarcode(it: String?) {
        uiScope.launch {
            if (it != null) {
                barcode.value = it
            }
        }

    }
}
