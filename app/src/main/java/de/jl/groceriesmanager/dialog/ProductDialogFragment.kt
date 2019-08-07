package de.jl.groceriesmanager.dialog

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.DialogProductBinding
import de.jl.tools.openDatePicker
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity





class ProductDialogFragment(
    private val prodId: Long = 0L,
    private val expiryDateString: String = ""
) : DialogFragment() {

    lateinit var productDialogBinding: DialogProductBinding
    lateinit var application: Application
    lateinit var productDialogViewModel: ProductDialogViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_product, container);
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
        val viewModelFactory = GroceriesManagerViewModelFactory(application, prodId, expiryDateString)

        //ViewModel
        productDialogViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductDialogViewModel::class.java)

        productDialogBinding.lifecycleOwner = this
        productDialogBinding.viewModel = productDialogViewModel
        productDialogBinding.tietExpiryDateString.setOnClickListener {
            context?.let {
                openDatePicker(it, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val expiryDateString = """$dayOfMonth.${month + 1}.$year"""
                    productDialogViewModel.expiryDateString.value = expiryDateString

                })
            }
        }
        if(prodId>0L){
            productDialogBinding.title.text = getString(R.string.modify_product)
            productDialogBinding.confirmProductBtn.text = getString(R.string.save)
        }

        productDialogBinding.confirmProductBtn.setOnClickListener {
            onConfirmProductBtn()
        }

        setObservers()

        val dialog = AlertDialog.Builder(activity as Context).setView(productDialogBinding.root).create()
        dialog.window.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun setObservers() {
        productDialogViewModel.productDescription.observe(this, Observer {
            it?.let{
                productDialogViewModel.checkProductValid()
            }
        })

        productDialogViewModel.expiryDateString.observe(this, Observer {
            it?.let{
                productDialogViewModel.checkProductValid()
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
                sendResult(it, productDialogViewModel.expiryDateString.value.toString())
            }
        })
    }


    private fun sendResult(id: Long, expDate: String) {
        val bundle = Bundle()
        bundle.putLong("ProdId",id)
        bundle.putString("ExpDate", expDate)
        val intent = Intent().putExtras(bundle)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    private fun onConfirmProductBtn() {
        productDialogViewModel.confirmProductBtnClicked()
    }
}