package de.jl.groceriesmanager.inventory

import android.app.Application
import android.app.DatePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.dialog.ProductDialogFragment
import de.jl.tools.openDialogNewProduct
import kotlinx.android.synthetic.main.dialog_product.view.*
import android.R




class InventoryFragment : Fragment() {

    lateinit var inventoryBinding: de.jl.groceriesmanager.databinding.FragmentInventoryBinding
    lateinit var application: Application
    lateinit var viewModelFactory: GroceriesManagerViewModelFactory
    lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            (activity as AppCompatActivity).supportActionBar?.title = getString(de.jl.groceriesmanager.R.string.common_inventory)
            //Binding
            inventoryBinding = DataBindingUtil.inflate(inflater, de.jl.groceriesmanager.R.layout.fragment_inventory, container, false)

            //Application
            application = requireNotNull(this.activity).application


            //DataSources

            //ViewModelFactory
            viewModelFactory = GroceriesManagerViewModelFactory(application)

            //ViewModel
            inventoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(InventoryViewModel::class.java)

            //Adapter für RecyclerView
            val adapter = InventoryItemAdapter(InventoryItemListener { inventoryId ->
                inventoryViewModel.modifyProduct(inventoryId)
            })

            inventoryBinding.lifecycleOwner = this
            inventoryBinding.inventoryViewModel = inventoryViewModel
            inventoryBinding.inventoryItemList.adapter = adapter
            inventoryBinding.inventoryItemList.layoutManager = GridLayoutManager(activity, 1)
            inventoryBinding.insertNewProductBtn.setOnClickListener { openAddProductDialog() }
            setObservers(adapter)
            validateArguments()

            return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(de.jl.groceriesmanager.R.layout.fragment_inventory, container, false)
    }

    private fun openAddProductDialog() {
        context?.let {
            val productDialogFragment = ProductDialogFragment()
            fragmentManager?.let { it1 -> productDialogFragment.show(it1,"test") }
            //openDialogNewProduct(context!!, View.OnClickListener{
            //    val expiryDateString = it.tiet_expiryDateString.text.toString()
            //    inventoryViewModel.newProductInserted(Pair(0L,expiryDateString))
            //})
        }
    }

    private fun validateArguments() {
        try {
            val args: InventoryFragmentArgs by navArgs()
            val productId = args.productId
            val expiryDateString = args.expiryDateString
            if (productId > 0 && !expiryDateString.isNullOrEmpty()) {
                inventoryViewModel.newProductInserted(Pair(productId, expiryDateString))
            }

        } catch (e: Exception) {
            Log.e("InventoryFragment", "Failed to validate Args: " + e.localizedMessage)
        }
    }
    private fun setObservers(adapter: InventoryItemAdapter) {
        //Observer für das Navigieren zum AddProduct-Screen
        inventoryViewModel.navigateToAddProduct.observe(this, Observer { pair ->
            pair?.let {
                this.findNavController()
                    .navigate(
                        InventoryFragmentDirections.inventoryDestinationToAddProductDestination(
                            pair.first,
                            pair.second
                        )
                    )
                inventoryViewModel.doneNavigatingToAddProduct()
            }
        })

        //Observer für Hinzufügen eines neuen InventoryItems
        inventoryViewModel.newProductInventoryItem.observe(this, Observer { value ->
            value?.let {
                inventoryViewModel.addNewProductItem()
            }
        })

        //Observer für Löschen eines InventoryItems
        inventoryViewModel.removeInventoryItem.observe(this, Observer {
            inventoryViewModel.removeInventoryItem()
        })

        //Observer für Recycler-View Items
        inventoryViewModel.inventories.observe(this, Observer {
            it?.let {
                inventoryViewModel.fillInventoryProducts()
            }
        })

        inventoryViewModel.inventoriesWithProduct.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                inventoryViewModel.deleteInventoryItem(item.groupId.toLong())
            }
        }
        return true
    }
}