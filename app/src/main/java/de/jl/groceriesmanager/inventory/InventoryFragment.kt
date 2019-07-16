package de.jl.groceriesmanager.inventory

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.inventory.InventoryDao

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

            //ViewModelFactory
            viewModelFactory = InventoryViewModelFactory(dataSource, application)

            inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)

            return inventoryBinding.root
            //Binding
            //val inventoryBinding: InventoryFragmentBinding =
            //    DataBindingUtil.inflate(inflater, R.layout.fragment_inventory, container, false)
//
            //val application = requireNotNull(this.activity).application
            //val adapter = ItemAdapter()
            //val dataSource = GroceriesManagerDatabase.getInstance(application).inventoryDao
//
            //val viewModelFactory = InventoryViewModelFactory(dataSource, application)
//
            //val inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)
            //inventoryViewModel.navigateToAddItem.observe(this, Observer { navigateToAddItem ->
            //    navigateToAddItem?.let {
            //        if(navigateToAddItem){
            //            this.findNavController().navigate(
            //                InventoryFragmentDirections
            //                    .actionInventoryDestinationToAddItemFragment(null)
            //            )
            //            inventoryViewModel.doneNavigatingToAddItem()
            //        }
            //    }
            //})
//
            //inventoryViewModel.inventoryItems.observe(viewLifecycleOwner, Observer {
            //    it?.let {
            //        adapter.data = it
            //    }
            //})
//
            //inventoryBinding.lifecycleOwner = this
            //inventoryBinding.inventoryItemList.adapter = adapter
//
            //inventoryBinding.inventoryViewModel = inventoryViewModel
//
            //return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }





}