package de.jl.groceriesmanager.add_item


import android.app.Application
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDatabase

class AddItemFragment : Fragment() {

    lateinit var application: Application

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            val binding: de.jl.groceriesmanager.databinding.AddItemToInventoryBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.add_item_to_inventory, container, false
            )
            application = requireNotNull(this.activity).application
            val arguments = AddItemFragmentArgs.fromBundle(arguments!!)
            val dataSource = GroceriesManagerDatabase.getInstance(application).itemDao
            val viewModelFactory = AddItemViewModelFactory(arguments.item, dataSource)

            val addItemViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddItemViewModel::class.java)

            setObservers(addItemViewModel)

            binding.addItemViewModel = addItemViewModel
            binding.lifecycleOwner = this
            return binding.root

        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.grocery_list_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setObservers(addItemViewModel: AddItemViewModel) {
        addItemViewModel.navigatToInventory.observe(this, Observer { navigateToInventory ->
            navigateToInventory?.let {
                if (navigateToInventory) {
                    //1. Navigate
                    this.findNavController()
                        .navigate(AddItemFragmentDirections.actionAddItemFragmentToInventoryDestination())
                    //2. reset obs. property
                    addItemViewModel.doneNavigatingToInventory()
                }
            }
        })

        addItemViewModel.openExpiryDatePickerDialog.observe(this, Observer { openExpiryDatePickerDialog ->
            openExpiryDatePickerDialog?.let {
                if (openExpiryDatePickerDialog) {
                    //1. Open Date Picker
                    //2. Set value

                    //3. reset obs. property
                    addItemViewModel.doneExpiryDatePickerDialog()
                }
            }
        })

        addItemViewModel.itemDescription.observe(this, Observer { itemDescription ->
            itemDescription?.let {
                //1. validate Item
                addItemViewModel.validateItem()
            }
        })



    }
}
