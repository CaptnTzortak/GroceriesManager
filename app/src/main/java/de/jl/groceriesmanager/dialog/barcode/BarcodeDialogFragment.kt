package de.jl.groceriesmanager.dialog.barcode

import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.products.Barcode
import de.jl.groceriesmanager.databinding.DialogBarcodeBinding
import de.jl.tools.openDatePicker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_inventory.*


class BarcodeDialogFragment(barcode: Barcode) : DialogFragment() {

    var bCode: Barcode = barcode
    lateinit var barcodeDialogBinding: DialogBarcodeBinding
    lateinit var application: Application
    lateinit var barcodeDialogViewModel: BarcodeDialogViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_barcode, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.attributes?.windowAnimations = R.style.AppTheme_DialogAnimation
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        barcodeDialogBinding =
            DataBindingUtil.inflate(activity?.layoutInflater!!, R.layout.dialog_barcode, container, false)

        //Application
        application = requireNotNull(this.activity).application

        //ViewModelFactory
        val viewModelFactory = GroceriesManagerViewModelFactory(application, passedBarcode = bCode)

        //ViewModel
        barcodeDialogViewModel = ViewModelProviders.of(this, viewModelFactory).get(BarcodeDialogViewModel::class.java)

        barcodeDialogBinding.lifecycleOwner = this
        barcodeDialogBinding.viewModel = barcodeDialogViewModel

        barcodeDialogBinding.referenceProductBtn.setOnClickListener{
            barcodeDialogViewModel.referenceProductBtnClicked()
        }

        setObservers()

        val dialog = AlertDialog.Builder(activity as Context).setView(barcodeDialogBinding.root).create()
        dialog.window.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun setObservers() {

    }

    private fun sendResult(id: Long, expDate: String, finNote: String, quantityString: String?) {
        val bundle = Bundle()
        bundle.putLong("ProdId", id)
        bundle.putString("ExpDate", expDate)
        bundle.putString("Note", finNote)
        val q = quantityString ?: "1"
        bundle.putInt("Quantity", Integer.parseInt(q))
        val intent = Intent().putExtras(bundle)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

}