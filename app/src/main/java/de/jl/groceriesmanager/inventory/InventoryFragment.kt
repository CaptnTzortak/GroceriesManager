package de.jl.groceriesmanager.inventory

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
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDatabase
import de.jl.groceriesmanager.databinding.InventoryFragmentBinding

class InventoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            //Binding
            val inventoryBinding: InventoryFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.inventory_fragment, container, false)

            val application = requireNotNull(this.activity).application

            val dataSource = GroceriesManagerDatabase.getInstance(application).inventoryDao

            val viewModelFactory = InventoryViewModelFactory(dataSource, application)

            val inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)

            inventoryViewModel.navigateToAddItem.observe(this, Observer { item ->
                item?.let {
                    this.findNavController().navigate(
                        InventoryFragmentDirections
                            .actionInventoryDestinationToAddItemFragment(item.itemId)
                    )
                    inventoryViewModel.doneNavigatingToAddItem()
                }
            })

            inventoryBinding.lifecycleOwner = this
            inventoryBinding.inventoryViewModel = inventoryViewModel

            return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.inventory_fragment, container, false)
    }
}