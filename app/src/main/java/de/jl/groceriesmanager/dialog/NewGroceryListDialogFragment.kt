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
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import de.jl.groceriesmanager.databinding.DialogNewGroceryListBinding

class NewGroceryListDialogFragment(private val glId: Long = 0L) : DialogFragment() {

    lateinit var newGroceryListDialogBinding: DialogNewGroceryListBinding
    lateinit var application: Application
    lateinit var newGroceryListDialogViewModel: NewGroceryListDialogViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_new_grocery_list, container);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.attributes?.windowAnimations = R.style.AppTheme_DialogAnimation
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        newGroceryListDialogBinding =
            DataBindingUtil.inflate(activity?.layoutInflater!!, R.layout.dialog_new_grocery_list, container, false)

        //Application
        application = requireNotNull(this.activity).application

        //ViewModelFactory
        val viewModelFactory = GroceriesManagerViewModelFactory(application, 0L, "", glId)

        //ViewModel
        newGroceryListDialogViewModel =
            ViewModelProviders.of(this, viewModelFactory).get(NewGroceryListDialogViewModel::class.java)

        newGroceryListDialogBinding.lifecycleOwner = this
        newGroceryListDialogBinding.viewModel = newGroceryListDialogViewModel

        newGroceryListDialogBinding.confirmGroceryListBtn.setOnClickListener {
            onConfirmGroceryListBtn()
        }

        if(glId > 0L){
            //TODO: Save-Icon downloaden und hier statt dem "+" setzen
            //TODO: Obriges auch im Add/Edit ProductDialogFragment
            newGroceryListDialogBinding.confirmGroceryListBtn.text = getString(R.string.save)
            newGroceryListDialogBinding.title.text = getString(R.string.modify_groceryList)
        }

        setObservers()

        val dialog = AlertDialog.Builder(activity as Context).setView(newGroceryListDialogBinding.root).create()
        dialog.window.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun setObservers() {
        newGroceryListDialogViewModel.groceryListName.observe(this, Observer {
            it?.let {
                newGroceryListDialogViewModel.checkGroceryListNameValid()
            }
        })

        newGroceryListDialogViewModel.confirmProduct.observe(this, Observer {
            it?.let {
                if (it) {
                    newGroceryListDialogViewModel.insertOrUpdateGroceryList()
                }
            }
        })

        newGroceryListDialogViewModel.addedProduct.observe(this, Observer {
            it?.let {
                dismiss()
            }
        })
    }

    private fun onConfirmGroceryListBtn() {
        newGroceryListDialogViewModel.confirmGroceryListBtnClicked()
    }
}