package de.jl.groceriesmanager.product.add.groceryList

import android.app.Application
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
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.databinding.FragmentAddProductGroceryListBinding

class AddProductGroceryListFragment : Fragment() {

    lateinit var addProductGroceryListBinding: FragmentAddProductGroceryListBinding
    lateinit var application: Application
    lateinit var dataSource: ProductsDao
    lateinit var viewModelFactory: AddProductGroceryListViewModelFactory
    lateinit var viewModel : AddProductGroceryListViewModel
    var glId = 0L
    var prodId = 0L
    //private val args: FragmentAddProductGroceryList by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            //Binding
            addProductGroceryListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_product_grocery_list, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //val productId = args.prodId
            val args: AddProductGroceryListFragmentArgs by navArgs()
            glId = args.glId
            prodId = args.prodId

            //TODO: Adapter:
            //val adapter = ItemAdapter()

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).productsDao

            //ViewModelFactory
            viewModelFactory = AddProductGroceryListViewModelFactory(dataSource, application)

            viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddProductGroceryListViewModel::class.java)

            addProductGroceryListBinding.lifecycleOwner = this
            addProductGroceryListBinding.viewModel = viewModel
            addProductGroceryListBinding.executePendingBindings()

            //Observer
            setObserver()

            return addProductGroceryListBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    private fun setObserver() {
        viewModel.product.observe(this, Observer {
            it?.let {
                this.findNavController()
                    .navigate(AddProductGroceryListFragmentDirections.addProductGroceryListDestinationToGroceryListDestination(it,
                        viewModel.note.value.toString(), glId
                    ))
                viewModel.doneNavigatingToGroceryList()
            }
        })
    }

}