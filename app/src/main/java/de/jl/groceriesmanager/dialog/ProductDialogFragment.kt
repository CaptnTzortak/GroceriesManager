package de.jl.groceriesmanager.dialog

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.inventory.InventoryItemAdapter
import de.jl.groceriesmanager.inventory.InventoryItemListener
import de.jl.groceriesmanager.inventory.InventoryViewModel
import kotlinx.android.synthetic.main.dialog_product.view.*

class ProductDialogFragment : DialogFragment() {
    lateinit var productDialogBinding: de.jl.groceriesmanager.databinding.DialogProductBinding
    lateinit var application: Application
    lateinit var viewModelFactory: GroceriesManagerViewModelFactory
    lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            //Binding
            val productDialogBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_product, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //DataSources

            //ViewModelFactory
            viewModelFactory = GroceriesManagerViewModelFactory(application)

            //ViewModel
            //inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)
//
            ////Adapter für RecyclerView
            //val adapter = InventoryItemAdapter(InventoryItemListener { inventoryId ->
            //    inventoryViewModel.modifyProduct(inventoryId)
            //})

            productDialogBinding.lifecycleOwner = this
           //inventoryBinding.inventoryViewModel = inventoryViewModel
           //inventoryBinding.inventoryItemList.adapter = adapter
           //inventoryBinding.inventoryItemList.layoutManager = GridLayoutManager(activity, 1)
            //setObservers(adapter)
            //validateArguments()

            return productDialogBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }
}