package de.jl.groceriesmanager.inventory

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.database.inventory.Inventory

@BindingAdapter("itemBarcode")
fun TextView.setItemBarcode(item: Inventory) {
    val barcode = if (item.product!!.barcodeId > 0L) {
        item.product!!.barcodeId
    } else {
        item.product!!.id
    }
    text = "Barcode: $barcode"
}

@BindingAdapter("itemDescription")
fun TextView.setItemDescription(item: Inventory) {
    val product = item.product
    text = if (product == null || product.getDescription().isEmpty()) {
        "No Description"
    } else {
        product.getDescription()
    }
}


@BindingAdapter("productExpiryDate")
fun TextView.setProductExpiryDate(item: Inventory) {
    var value = "Verfallsdatum: "
    val secVal = item.expiryDateString
    value += secVal
    text = value
}
