package de.jl.groceriesmanager.grocery_list

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.database.gl_item_mapping.GLItemMapping

@BindingAdapter("glItemDescription")
fun TextView.setGlItemDescription(item: GLItemMapping?) {
    text = item?.product?.user_Description
}

@BindingAdapter("glItemNote")
fun TextView.setGlItemNote(item: GLItemMapping?) {
    var value = "Notiz: "
    val secVal= item?.note?: "no note"
    value += secVal
    text = value
}
