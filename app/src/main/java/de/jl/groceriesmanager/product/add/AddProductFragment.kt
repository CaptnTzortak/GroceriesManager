package de.jl.groceriesmanager.product.add

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.databinding.FragmentAddProductBinding
import de.jl.tools.openDatePicker

class AddProductFragment : Fragment() {

    lateinit var addProductBinding: FragmentAddProductBinding
    lateinit var application: Application
    lateinit var prodDB: ProductsDao
    lateinit var viewModelFactory: GroceriesManagerViewModelFactory
    lateinit var addProductViewModel: AddProductViewModel
    private val args: AddProductFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            //Binding
            addProductBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_product, container, false)

            //Application
            application = requireNotNull(this.activity).application

            val prodId = args.prodId
            val expiryDateString = args.expiryDateString

            //DataSource
            prodDB = GroceriesManagerDB.getInstance(application).productsDao

            //ViewModelFactory
            viewModelFactory = GroceriesManagerViewModelFactory(application, prodId, expiryDateString)

            addProductViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddProductViewModel::class.java)

            addProductBinding.lifecycleOwner = this
            addProductBinding.addProductViewModel = addProductViewModel
            addProductBinding.expiryDateBtn.setOnClickListener { openDatePickerClicked() }
            addProductBinding.executePendingBindings()

            //Observer
            setObserver()

            return addProductBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    private fun setObserver() {
        addProductViewModel.productIdWithExpiryDate.observe(this, Observer { it ->
            it?.let {
                val prodId = it.first
                val expiryDate = it.second

                this.findNavController()
                    .navigate(
                        AddProductFragmentDirections.addProductDestinationToInventoryDestination(
                            prodId,
                            expiryDate
                        )
                    )
                addProductViewModel.doneConfirmItem()
            }
        })

        addProductViewModel.description.observe(this, Observer { desc ->
            desc?.let {
                addProductViewModel.validateProduct()
            }
        })

        addProductViewModel.expiryDateString.observe(this, Observer { expiryDateString ->
            expiryDateString?.let {
                addProductViewModel.validateProduct()
            }
        })
    }

    private fun openDatePickerClicked() {
        context?.let {
            openDatePicker(it, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                addProductViewModel.expiryDateString.value = """$dayOfMonth.${month + 1}.$year"""
            })
        }
    }
}