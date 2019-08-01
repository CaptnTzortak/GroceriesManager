package de.jl.groceriesmanager.inventory

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.database.inventory.Inventory

@BindingAdapter("itemBarcode")
fun TextView.setItemBarcode(item: Inventory) {
    val barcode = if (item.product.barcodeId == 0L) {
        item.product.id
    } else {
        item.product.barcodeId
    }
    text = "Barcode: $barcode"
}

@BindingAdapter("itemDescription")
fun TextView.setItemDescription(item: Inventory) {
    val product = item.product
    text = if (product.description.isNullOrEmpty()) {
        "No Description"
    } else {
        product.description
    }
}

@BindingAdapter("productExpiryDate")
fun TextView.setProductExpiryDate(item: Inventory) {
    var value = "Verfallsdatum: "
    val secVal = item.expiryDateString
    value += secVal
    text = value
}
