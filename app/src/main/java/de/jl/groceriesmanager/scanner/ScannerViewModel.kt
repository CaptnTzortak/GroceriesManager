package de.jl.groceriesmanager.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import de.jl.groceriesmanager.database.products.Product
import de.jl.openfoodfacts.OpenFoodFactsProperty
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.net.URL


class ScannerViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _response = MutableLiveData<Product>()
    val response: LiveData<Product>
        get() = _response

    private val _valid = MutableLiveData<Boolean>()
    val valid: LiveData<Boolean>
        get() = _valid


    private val _scannedBarcode = MutableLiveData<String>()
    val scannedBarcode: LiveData<String>
        get() = _scannedBarcode


    var barcodeString = MutableLiveData<String>()

    var apiBarcode = ""

    init {
        barcodeString.value = ""
        Log.i("ScannerViewModel", "init")
    }

    private suspend fun getResult(): String {
        return withContext(Dispatchers.IO) {
            var result = ""
            try {
                result = URL("https://world-de.openfoodfacts.org/api/v0/product/$apiBarcode.json").readText()
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

    fun validateBarcode(barcode: String) {
        uiScope.launch {
            var isValid = false
            if (barcode.length == 8 || barcode.length == 13) {
                isValid = true
                apiBarcode = barcode
            }
            _valid.value = isValid
        }
    }

    fun getData() {
        uiScope.launch {
            _response.value = getProductByOpenFoodFactsJSON()
        }
    }

    private suspend fun getProductByOpenFoodFactsJSON(): Product? {
        val oFFProperty = withContext(Dispatchers.IO) {
            val result = getResult()
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(OpenFoodFactsProperty::class.java)
            adapter.fromJson(result)
        }
        return parseOFFProductToProduct(oFFProperty)
    }

    private suspend fun parseOFFProductToProduct(oFFProperty: OpenFoodFactsProperty?): Product? {
        try {
            if (oFFProperty != null && oFFProperty.status == 1) {
                //JSON-Success
                if (!oFFProperty.product.product_name.isNullOrEmpty()) {
                    val barcodeId = oFFProperty.product.id.toLong()
                    val productName = oFFProperty.product.product_name
                    var brands = oFFProperty.product.brands
                    var quantity = oFFProperty.product.quantity
                    var image = oFFProperty.product.image_url?.let { getBitmapByUrl(it) }

                    if (brands.isNullOrEmpty() || brands == "null") {
                        brands = ""
                    }
                    if (quantity.isNullOrEmpty() || quantity == "null") {
                        quantity = ""
                    }
                    return Product(0L, barcodeId, productName, quantity, brands, image)
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
                barcodeString.value = it
            }
        }

    }
}
