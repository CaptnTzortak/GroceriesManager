package de.jl.groceriesmanager.grocery_list

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts

@BindingAdapter("glItemDescription")
fun TextView.setGlItemDescription(item: GroceryListsProducts?) {
    text = item?.product?.description
}

@BindingAdapter("glItemNote")
fun TextView.setGlItemNote(item: GroceryListsProducts?) {
    var value = "Notiz: "
    val secVal = if(item?.note.isNullOrEmpty()){
        "no note"
    } else {
        item?.note
    }
    value += secVal
    text = value
}


@BindingAdapter("glItemQuantity")
fun TextView.setGlItemQuantity(item: GroceryListsProducts?) {
    text = "Quantity: ${item?.quantity}"
}