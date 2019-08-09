package de.jl.groceriesmanager.scanner

import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.database.products.Barcode

@BindingAdapter("barcodeTitle")
fun TextView.setBarcodeTitle(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.commonName
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeId")
fun TextView.setBarcodeId(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.id.toString()
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeProductName")
fun TextView.setBarcodeProductName(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.productName
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeCommonName")
fun TextView.setBarcodeCommonName(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.commonName
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeQuantity")
fun TextView.setBarcodeQuantity(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.quantity
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeBrands")
fun TextView.setBarcodeBrands(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.brands
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeCategories")
fun TextView.setBarcodeCategories(barcode: Barcode?) {
    text = if (barcode != null) {
        barcode.categories
    } else {
        "n/V"
    }
}

@BindingAdapter("barcodeImg")
fun ImageView.setBarcodeImg(barcode: Barcode?) {
    if(barcode!=null) {
        val bitmap = BitmapFactory.decodeByteArray(barcode.image, 0, barcode.image!!.size)
        setImageBitmap(bitmap)
    }
}
