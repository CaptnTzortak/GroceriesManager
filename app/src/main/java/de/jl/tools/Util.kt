package de.jl.tools

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import de.jl.groceriesmanager.R
import kotlinx.android.synthetic.main.dialog_new_grocery_list.*
import java.util.*


fun openDatePicker(context: Context, validateDate: DatePickerDialog.OnDateSetListener) {
    try {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { v, y, m, doM ->
            validateDate.onDateSet(v, y, m, doM)
        }, year, month, day)
        dpd.datePicker.minDate = c.timeInMillis

        dpd.show()
    } catch (e: java.lang.Exception) {
        Log.d("openDatePicker", e.localizedMessage)
    }
}


fun openDialogNewGroceryList(context: Context, listener: View.OnClickListener) {
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.dialog_new_grocery_list)
    dialog.window.attributes.windowAnimations = R.style.AppTheme_DialogAnimation
    dialog.window.setBackgroundDrawable(ColorDrawable(0))
    dialog.addGroceryListBtn.setOnClickListener {
        listener.onClick(it)
    }
    dialog.show()
}

fun openDialogNewProduct(context: Context, listener: View.OnClickListener) {
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.dialog_product)
    dialog.window.attributes.windowAnimations = R.style.AppTheme_DialogAnimation
    dialog.window.setBackgroundDrawable(ColorDrawable(0))
    dialog.tiet_expiryDateString.setOnClickListener {
        openDatePicker(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val expiryDateString = """$dayOfMonth.${month + 1}.$year"""
            dialog.tiet_expiryDateString.setText(expiryDateString)
            }
        )
    }
    dialog.addProductBtn.setOnClickListener {
        listener.onClick(it)
    }
    dialog.show()
}