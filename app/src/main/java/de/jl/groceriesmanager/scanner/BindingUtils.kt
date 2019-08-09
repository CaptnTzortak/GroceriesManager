package de.jl.groceriesmanager.scanner

import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.database.products.Barcode


@BindingAdapter("barcodeText")
fun TextView.setBarcodeText(barcode: String?) {
    var rBarcode = barcode
    if (rBarcode != null) {
        while (rBarcode!!.length < 13) {
            rBarcode = "0$rBarcode"
        }
    }
    text = "Current barcode: $rBarcode"
}

@BindingAdapter("barcodeDescription")
fun TextView.setBarcodeDescription(barcode: Barcode?) {
    var value = if (barcode != null) {
        barcode.barcodeDescription
    } else {
        "n/V"
    }
    text = value
}


@BindingAdapter("barcodeCommonName")
fun TextView.setBarcodeCommonName(barcode: Barcode?) {
    var value = "CommonName: "
    value += if (barcode != null) {
        barcode.commonName
    } else {
        "n/V"
    }
    text = value
}


@BindingAdapter("barcodeQuantity")
fun TextView.setBarcodeQuantity(barcode: Barcode?) {
    var value = "Quantity: "
    value += if (barcode != null) {
        barcode.quantity
    } else {
        "n/V"
    }
    text = value
}


@BindingAdapter("barcodeBrands")
fun TextView.setBarcodeBrands(barcode: Barcode?) {
    var value = "Brands: "
    value += if (barcode != null) {
        barcode.brands
    } else {
        "n/V"
    }
    text = value
}


@BindingAdapter("barcodeCategories")
fun TextView.setBarcodeCategories(barcode: Barcode?) {
    var value = "Categories: "
    value += if (barcode != null) {
        barcode.categories
    } else {
        "n/V"
    }
    text = value
}


@BindingAdapter("barcodeImg")
fun ImageView.setBarcodeImg(barcode: Barcode?) {
    if(barcode!=null) {
        val bitmap = BitmapFactory.decodeByteArray(barcode.image, 0, barcode.image!!.size)
        setImageBitmap(bitmap)
    }
}