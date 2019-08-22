package de.jl.tools

import android.app.DatePickerDialog
import android.content.ClipDescription
import android.content.Context
import android.util.Log
import java.util.*

object CONST {
    const val TAG = "Util"
}

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
        Log.d(CONST.TAG, e.localizedMessage)
    }
}

fun parseProductDescriptionToProdName(description: String): String {
    val prodVals = description.split(" - ")
    if(prodVals.isNotEmpty()){
        return prodVals[0].trim()
    }
    return ""
}