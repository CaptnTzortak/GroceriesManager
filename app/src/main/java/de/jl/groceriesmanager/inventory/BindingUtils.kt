package de.jl.groceriesmanager.inventory

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.products.ProductItem

@BindingAdapter("itemBarcode")
fun TextView.setItemBarcode(prod: ProductItem?) {
    val barcode = if (prod?.barcode_id == 0L) {
        prod.product_id
    } else {
        prod?.barcode_id
    }
    text = "Barcode: " + barcode.toString()
}

@BindingAdapter("itemDescription")
fun TextView.setItemDescription(prod: ProductItem?) {
    text = prod?.user_Description ?: "No Description"
}

@BindingAdapter("productExpiryDate")
fun TextView.setProductExpiryDate(product: ProductItem?){
    var value = "Verfallsdatum: "
    val secVal= product?.expiry_date_string ?: "no expiry date"
    value += secVal
    text = value
}