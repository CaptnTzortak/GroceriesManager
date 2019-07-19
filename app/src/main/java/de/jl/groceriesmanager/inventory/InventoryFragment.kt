package de.jl.groceriesmanager.inventory

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
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.product.add.AddProductFragmentDirections

class InventoryFragment : Fragment() {

    lateinit var inventoryBinding: de.jl.groceriesmanager.databinding.FragmentInventoryBinding
    lateinit var application: Application
    lateinit var dataSource: InventoryDao
    lateinit var prodDataSource: ProductsDao
    lateinit var viewModelFactory: InventoryViewModelFactory
    lateinit var inventoryViewModel: InventoryViewModel
    val args: InventoryFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        try{
            //Binding
            inventoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_inventory, container, false)

            //Application
            application = requireNotNull(this.activity).application


            //Adapter für RecyclerView
            val adapter = InventoryItemAdapter()

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).inventoryDao
            prodDataSource = GroceriesManagerDB.getInstance(application).productsDao

            //ViewModelFactory
            viewModelFactory = InventoryViewModelFactory(dataSource, prodDataSource, application)

            //ViewModel
            inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)



            inventoryBinding.inventoryItemList.adapter = adapter
            inventoryBinding.lifecycleOwner = this
            inventoryBinding.inventoryViewModel = inventoryViewModel
            inventoryBinding.inventoryItemList.layoutManager = GridLayoutManager(activity,1 )

            val productId = args?.productId
            if(productId > 0 ){

                inventoryViewModel.insertNewInventoryItem(productId)
            }

            setObservers(adapter)

            return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    private fun setObservers(adapter: InventoryItemAdapter) {
        inventoryViewModel.navigateToAddProduct.observe(this, Observer { navToAddProduct ->
            navToAddProduct?.let {
                if (navToAddProduct) {
                    this.findNavController()
                        .navigate(InventoryFragmentDirections.inventoryDestinationToAddProductDestination())
                    inventoryViewModel.doneNavigatingToAddProduct()
                }
            }
        })


        inventoryViewModel.inventoryItems.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }
}