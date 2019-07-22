package de.jl.groceriesmanager.product.add

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.databinding.FragmentAddProductBinding
import java.util.*

class AddProductFragment : Fragment() {

    lateinit var addProductBinding: FragmentAddProductBinding
    lateinit var application: Application
    lateinit var dataSource: ProductsDao
    lateinit var viewModelFactory: AddProductViewModelFactory
    lateinit var addProductViewModel: AddProductViewModel
    private val args: AddProductFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            //Binding
            addProductBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_product, container, false)

            //Application
            application = requireNotNull(this.activity).application

            val productId = args.prodId

            //TODO: Adapter:
            //val adapter = ItemAdapter()

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).productsDao

            //ViewModelFactory
            viewModelFactory = AddProductViewModelFactory(dataSource, application, productId)

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
        addProductViewModel.product.observe(this, Observer { prodId ->
            prodId?.let {
                this.findNavController()
                    .navigate(AddProductFragmentDirections.addProductDestinationToInventoryDestination(prodId))
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

    fun openDatePickerClicked(){
        try{
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this.context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in Toast
                addProductViewModel.expiryDateString.value = """$dayOfMonth.${monthOfYear + 1}.$year"""
            }, year, month, day)
            dpd.show()}
        catch (e: java.lang.Exception){
            Log.d("AddProductViewModel", e.localizedMessage)
        }
    }

}