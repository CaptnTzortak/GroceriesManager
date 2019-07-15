package de.jl.groceriesmanager.add_item


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
import de.jl.groceriesmanager.inventory.InventoryFragmentDirections

class AddItemFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            val binding: de.jl.groceriesmanager.databinding.AddItemToInventoryBinding = DataBindingUtil.inflate(inflater,
                    R.layout.add_item_to_inventory, container, false)
            val application = requireNotNull(this.activity).application
            val arguments = AddItemFragmentArgs.fromBundle(arguments!!)
            val dataSource = GroceriesManagerDatabase.getInstance(application).itemDao
            val viewModelFactory = AddItemViewModelFactory(arguments.itemId, dataSource)

            val addItemViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddItemViewModel::class.java)

            addItemViewModel.navigateToIntventory.observe(this,  Observer { item ->
                item?.let {
                    this.findNavController().navigate(
                        AddItemFragmentDirections.actionAddItemFragmentToInventoryDestination()
                    )
                    addItemViewModel.doneNavigating()
                }
            })


            binding.addItemViewModel = addItemViewModel
            return binding.root

        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.grocery_list_fragment, container, false)
    }
}
