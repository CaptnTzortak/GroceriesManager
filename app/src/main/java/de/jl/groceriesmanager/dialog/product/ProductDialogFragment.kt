package de.jl.groceriesmanager.dialog.product

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.DialogProductBinding
import de.jl.tools.openDatePicker
import de.jl.tools.parseProductDescriptionToProdName
import kotlinx.android.synthetic.main.activity_main.*


class ProductDialogFragment(
    private val invId: Long = 0L,
    private val prodId: Long = 0L,
    private val expiryDateString: String? = null,
    private val note: String? = null
) : DialogFragment() {

    private lateinit var productDialogBinding: DialogProductBinding
    private lateinit var application: Application
    private lateinit var productDialogViewModel: ProductDialogViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_product, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.attributes?.windowAnimations = R.style.AppTheme_DialogAnimation
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        productDialogBinding =
            DataBindingUtil.inflate(activity?.layoutInflater!!, R.layout.dialog_product, container, false)

        //Application
        application = requireNotNull(this.activity).application

        //ViewModelFactory
        val viewModelFactory = GroceriesManagerViewModelFactory(application, prodId, expiryDateString, 0L, note)

        //ViewModel
        productDialogViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductDialogViewModel::class.java)

        productDialogBinding.lifecycleOwner = this
        productDialogBinding.viewModel = productDialogViewModel
        productDialogBinding.tietExpiryDateString.setOnClickListener {
            context?.let {
                openDatePicker(it, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val rMonth = month + 1
                    val realMonth = if (rMonth < 10) {
                        "0$rMonth"
                    } else {
                        "$rMonth"
                    }
                    val expiryDateString = """$dayOfMonth.$realMonth.$year"""
                    productDialogViewModel.expiryDateString.value = expiryDateString

                })
            }
        }
        if (prodId > 0L) {
            productDialogBinding.title.text = getString(R.string.modify_product)
            productDialogBinding.confirmProductBtn.text = getString(R.string.btn_save)
            productDialogBinding.confirmProductBtn.icon = ContextCompat.getDrawable(this.context!!,R.drawable.ic_save)
        }

        productDialogBinding.confirmProductBtn.setOnClickListener {
            onConfirmProductBtn()
        }

        productDialogBinding.productNameAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val productDesc = parent.getItemAtPosition(position).toString()
            onProductNameAutoCompleteItemClick(productDesc)
        }

        setObservers()

        val dialog = AlertDialog.Builder(activity as Context).setView(productDialogBinding.root).create()
        if (dialog != null && dialog.window != null) {
            val rWindow = dialog.window
            if (rWindow != null) {
                rWindow.setBackgroundDrawable(ColorDrawable(0))
                rWindow.requestFeature(Window.FEATURE_NO_TITLE)
            }
        }
        return dialog
    }

    private fun onProductNameAutoCompleteItemClick(prodDescription: String) {
        productDialogViewModel.checkIfProductAlreadyExists(parseProductDescriptionToProdName(prodDescription))
    }

    private fun setObservers() {
        productDialogViewModel.existingProductDescriptions.observe(this, Observer {
            it?.let {
                val prodDesctriptions = mutableListOf<String>()
                it.forEach { product ->
                    prodDesctriptions.add(product.getDescription())
                }
                context?.let { cont ->
                    productDialogBinding.productNameAutoComplete.setAdapter(
                        ArrayAdapter(
                            cont,
                            R.layout.dropdown_menu_popup_item,
                            prodDesctriptions
                        )
                    )
                }

            }
        })

        productDialogViewModel.productNameString.observe(this, Observer {
            it?.let {
                productDialogViewModel.checkProductValid()
            }
        })

        productDialogViewModel.expiryDateString.observe(this, Observer {
            if (it != null) {
                productDialogBinding.tilExpiryDateString.visibility = View.VISIBLE
                productDialogBinding.tilNote.visibility = View.GONE
                productDialogViewModel.checkProductValid()
            } else {
                productDialogBinding.tilExpiryDateString.visibility = View.GONE
                productDialogBinding.tilNote.visibility = View.VISIBLE
            }
        })

        productDialogViewModel.note.observe(this, Observer {
            if (it != null) {
                productDialogBinding.tilNote.visibility = View.VISIBLE
                productDialogBinding.tilExpiryDateString.visibility = View.GONE
                productDialogViewModel.checkProductValid()
            } else {
                productDialogBinding.tilExpiryDateString.visibility = View.VISIBLE
                productDialogBinding.tilNote.visibility = View.GONE
            }
        })

        productDialogViewModel.confirmProduct.observe(this, Observer {
            it?.let {
                if (it) {
                    productDialogViewModel.insertOrUpdateProduct()
                }
            }
        })

        productDialogViewModel.addedProduct.observe(this, Observer {
            it?.let {
                sendResult(
                    it,
                    productDialogViewModel.expiryDateString.value.toString(),
                    productDialogViewModel.note.value.toString()
                )
            }
        })
    }


    private fun sendResult(id: Long, expDate: String, finNote: String) {
        val bundle = Bundle()
        bundle.putLong("ProdId", id)
        bundle.putString("ExpDate", expDate)
        bundle.putString("Note", finNote)
        if (invId > 0L) {
            bundle.putLong("InvId", invId)
        }
        val intent = Intent().putExtras(bundle)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun onConfirmProductBtn() {
        productDialogViewModel.confirmProductBtnClicked()
    }
}