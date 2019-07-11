package de.jl.groceriesmanager.inventory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.InventoryFragmentBinding

class InventoryFragment : Fragment() {

    private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var inventoryViewModelFactory: InventoryViewModelFactory
    private lateinit var inventoryBinding: InventoryFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        try{
            //Binding
            inventoryBinding = DataBindingUtil.inflate(inflater, R.layout.inventory_fragment, container, false)

            //ViewModelFactory
            //mit Parameter:
            //inventoryViewModelFactory = InventoryViewModelFactory(InventoryFragmentArgs.fromBundle(arguments!!).score)
            //Ohne Parameter:
            inventoryViewModelFactory = InventoryViewModelFactory()
            //ViewModel
            inventoryViewModel = ViewModelProviders.of(this, inventoryViewModelFactory).get(InventoryViewModel::class.java)

            return inventoryBinding.root

        } catch(e: Exception){
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.inventory_fragment, container, false)
    }
}