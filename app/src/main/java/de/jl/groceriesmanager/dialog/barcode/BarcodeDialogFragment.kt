package de.jl.groceriesmanager.dialog.barcode

import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.products.Product
import de.jl.groceriesmanager.databinding.DialogBarcodeBinding
import de.jl.tools.openDatePicker
import kotlinx.android.synthetic.main.activity_main.*


class BarcodeDialogFragment(product: Product) : DialogFragment() {

    companion object {
        const val TAG = "BarcodeDialogFragment"
    }

    var prod = product
    lateinit var barcodeDialogBinding: DialogBarcodeBinding
    lateinit var application: Application
    lateinit var barcodeDialogViewModel: BarcodeDialogViewModel
    private var _existingProductNamesWithoutBarcode: List<String> = emptyList()
    private var _barcodeAlreadyReferenced: Int = 0
    private var _existingGroceryListNames: List<String> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            return inflater.inflate(R.layout.dialog_barcode, container)
        } catch (e: Exception) {
            Log.e("$TAG - onCreateView", e.localizedMessage)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog?.window?.attributes?.windowAnimations = R.style.AppTheme_DialogAnimation
            dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
        } catch (e: Exception) {
            Log.e("$TAG - onViewCreated", e.localizedMessage)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        try {
            barcodeDialogBinding =
                DataBindingUtil.inflate(activity?.layoutInflater!!, R.layout.dialog_barcode, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //ViewModelFactory
            val viewModelFactory = GroceriesManagerViewModelFactory(application, passedProduct = prod)

            //ViewModel
            barcodeDialogViewModel =
                ViewModelProviders.of(this, viewModelFactory).get(BarcodeDialogViewModel::class.java)

            barcodeDialogBinding.lifecycleOwner = this
            barcodeDialogBinding.viewModel = barcodeDialogViewModel

            barcodeDialogBinding.referenceToProductBtn.setOnClickListener {
                referenceToProductBtnClicked()
            }

            barcodeDialogBinding.addToInventoryBtn.setOnClickListener {
                addToInventoryBtnClicked()
            }

            barcodeDialogBinding.addToGLBtn.setOnClickListener {
                addToGLBtnClicked()
            }
            setObservers()
            retainInstance = true
            val dialog = AlertDialog.Builder(activity as Context).setView(barcodeDialogBinding.root).create()
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            return dialog
        } catch (e: Exception) {
            Log.e("$TAG - onCreateDialog", e.localizedMessage)
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun addToGLBtnClicked() {
        try {
            if (_existingGroceryListNames.isNotEmpty()) {
                val builder = context?.let { AlertDialog.Builder(it) }
                if (builder != null) {
                    builder.setTitle("Select Grocery List")
                    builder.setItems(_existingGroceryListNames.toTypedArray()) { _, which ->
                        Toast.makeText(context, _existingGroceryListNames[which] + " is clicked", Toast.LENGTH_SHORT)
                            .show()
                        barcodeDialogViewModel.addBarcodeAsProductAndGroceryListEntry(_existingGroceryListNames[which])
                    }
                    builder.setNegativeButton(
                        android.R.string.cancel,
                        DialogInterface.OnClickListener { _, _ -> })
                    builder.show()
                }
            } else {
                Toast.makeText(context, getString(R.string.text_no_existing_grocery_lists), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("$TAG - addToGLBtnClicked", e.localizedMessage)
        }
    }

    private fun addToInventoryBtnClicked() {
        try {
            context?.let {
                openDatePicker(it, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    var rMonth = month + 1
                    if (rMonth == 13) {
                        rMonth = 1
                    }
                    val realMonth = if (rMonth < 10) {
                        "0$rMonth"
                    } else {
                        "$rMonth"
                    }
                    val expiryDateString = """$dayOfMonth.$realMonth.$year"""
                    barcodeDialogViewModel.addBarcodeAsProductAndInventory(expiryDateString)
                    sendResult(0L, "", "", "")
                })
            }
        } catch (e: Exception) {
            Log.e("$TAG - addToInventoryBtnClicked", e.localizedMessage)
        }
    }

    private fun referenceToProductBtnClicked() {
        try {
            if(_barcodeAlreadyReferenced == 0){
                Toast.makeText(context, getString(R.string.text_barcode_already_referenced), Toast.LENGTH_LONG).show()
            } else if (_existingProductNamesWithoutBarcode.isNotEmpty()) {
                val builder = context?.let { AlertDialog.Builder(it) }
                if (builder != null) {
                    builder.setTitle(getString(R.string.title_reference_barcode))
                    builder.setItems(_existingProductNamesWithoutBarcode.toTypedArray()) { _, which ->
                        Toast.makeText(
                            context,
                            getString(R.string.text_barcode_referenced_with_product).replace(
                                "{0}",
                                prod.barcodeId.toString()
                            )
                                .replace("{1}", _existingProductNamesWithoutBarcode[which]),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        barcodeDialogViewModel.referenceBarcodeWithProduct(_existingProductNamesWithoutBarcode[which])
                    }
                    builder.setNegativeButton(
                        android.R.string.cancel,
                        DialogInterface.OnClickListener { _, _ -> })

                    val dialog = builder.create()
                    val lv = dialog.listView
                    lv.divider = ContextCompat.getDrawable(this.context!!, R.drawable.rv_devider)
                    lv.dividerHeight = 1
                    dialog.show()
                }
            } else {
                Toast.makeText(context, getString(R.string.text_no_existing_products), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("$TAG - referenceToProductBtnClicked", e.localizedMessage)
        }
    }

    private fun setObservers() {
        try {
            barcodeDialogViewModel.existingProductNamesWithoutBarcode.observe(this, Observer {
                it?.let {
                    val prodDesctriptions = mutableListOf<String>()
                    it.forEach { product ->
                        prodDesctriptions.add(product.getDescription())
                    }
                    _existingProductNamesWithoutBarcode = prodDesctriptions
                }
            })

            barcodeDialogViewModel.barcodeAlreadyReferenced.observe(this, Observer {
                it?.let{
                    _barcodeAlreadyReferenced = it
                }
            })

            barcodeDialogViewModel.existingGroceryListNames.observe(this, Observer {
                it?.let {
                    _existingGroceryListNames = it
                }
            })
        } catch (e: Exception) {
            Log.e("$TAG - setObservers", e.localizedMessage)
        }
    }

    private fun sendResult(id: Long, expDate: String, finNote: String, quantityString: String?) {
        try {
            val bundle = Bundle()
            bundle.putLong("ProdId", id)
            bundle.putString("ExpDate", expDate)
            bundle.putString("Note", finNote)
            val q = if (quantityString.isNullOrEmpty() || quantityString == "null") {
                "1"
            } else {
                quantityString
            }
            bundle.putInt("Quantity", Integer.parseInt(q))
            val intent = Intent().putExtras(bundle)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        } catch (e: Exception) {
            Log.e("$TAG - sendResult", e.localizedMessage)
        }
    }

}