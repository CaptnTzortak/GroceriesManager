package de.jl.groceriesmanager.scanner

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("barcodeText")
fun TextView.setBarcodeText(barcode: String?) {
    var ret = "Current barcode: "
    ret += barcode ?: "XXXXXXXXXXXXXX"
    text = ret
}
