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
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.inventory.InventoryDao
import de.jl.groceriesmanager.database.inventory.InventoryItem
import de.jl.groceriesmanager.database.products.ProductsDao

class InventoryFragment : Fragment() {

    lateinit var inventoryBinding: de.jl.groceriesmanager.databinding.FragmentInventoryBinding
    lateinit var application: Application
    lateinit var dataSource: InventoryDao
    lateinit var viewModelFactory: InventoryViewModelFactory
    lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        try{
            //Binding
            inventoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_inventory, container, false)

            //Application
            application = requireNotNull(this.activity).application



            //TODO: Adapter:
            //val adapter = ItemAdapter()

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).inventoryDao


            val prodId: Long = arguments?.get("productId") as Long
            if(prodId > 0){
                val invItem = InventoryItem()
                invItem.product_id = prodId
                dataSource.insert(invItem)
            }


            //ViewModelFactory
            viewModelFactory = InventoryViewModelFactory(dataSource, application)

            inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)



            //Observing
            //TODO uncomment after adapter
            //inventoryViewModel.inventoryItems.observe(viewLifecycleOwner, Observer {
            //    it?.let {
            //        adapter.submitList(it)
            //    }
            //})

            //TODO uncomment after adapter
            //inventoryBinding.inventoryItemList.adapter = adapter

            inventoryViewModel.navigateToAddProduct.observe(this, Observer {navToAddProduct ->
                navToAddProduct?.let{
                    if(navToAddProduct){
                        this.findNavController().navigate(InventoryFragmentDirections.actionInventoryDestinationToAddProductDestination())
                        inventoryViewModel.doneNavigatingToAddProduct()
                    }
                }
            })

            inventoryBinding.lifecycleOwner = this
            inventoryBinding.inventoryViewModel = inventoryViewModel
            inventoryBinding.inventoryItemList.layoutManager = GridLayoutManager(activity,1 )

            return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }
}