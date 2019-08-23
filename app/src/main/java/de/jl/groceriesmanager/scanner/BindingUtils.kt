package de.jl.groceriesmanager.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.products.Product


@BindingAdapter("barcodeTitle")
fun TextView.setBarcodeTitle(product: Product?) {
    text = product?.getDescription() ?: "n/V"
}

@BindingAdapter("barcodeId")
fun TextView.setBarcodeId(product: Product?) {
    text = product?.barcodeId?.toString() ?: "n/V"
}

@BindingAdapter("barcodeProductName")
fun TextView.setBarcodeProductName(barcode: Product?) {
    text = barcode?.name ?: "n/V"
}

@BindingAdapter("barcodeQuantity")
fun TextView.setBarcodeQuantity(barcode: Product?) {
    text = barcode?.quantity ?: "n/V"
}

@BindingAdapter("barcodeBrands")
fun TextView.setBarcodeBrands(barcode: Product?) {
    text = barcode?.brand ?: "n/V"
}

@BindingAdapter("barcodeImg")
fun ImageView.setBarcodeImg(barcode: Product?) {
    val bitmap: Bitmap = if(barcode?.image != null) {
        BitmapFactory.decodeByteArray(barcode.image, 0, barcode.image!!.size)
    } else {
        BitmapFactory.decodeResource(resources, R.drawable.no_image_found)
    }
    setImageBitmap(bitmap)
}
