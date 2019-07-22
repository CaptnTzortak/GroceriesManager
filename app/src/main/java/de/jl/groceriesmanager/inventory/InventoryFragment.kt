package de.jl.groceriesmanager.inventory

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlin.math.absoluteValue

class InventoryFragment : Fragment() {

    lateinit var inventoryBinding: de.jl.groceriesmanager.databinding.FragmentInventoryBinding
    lateinit var application: Application
    lateinit var dataSource: InventoryDao
    lateinit var prodDataSource: ProductsDao
    lateinit var viewModelFactory: InventoryViewModelFactory
    lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            //Binding
            inventoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_inventory, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).inventoryDao
            prodDataSource = GroceriesManagerDB.getInstance(application).productsDao

            //ViewModelFactory
            viewModelFactory = InventoryViewModelFactory(dataSource, prodDataSource, application)

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

            setObservers(adapter)
            validateArguments()

            return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    private fun validateArguments() {
        try {
            val args: InventoryFragmentArgs by navArgs()
            val productId = args.productId
            if (productId > 0) {
                inventoryViewModel.newProductInserted(productId)
            }
        } catch (e: Exception) {
            Log.e("InventoryFragment", "Failed to validate Args: " + e.localizedMessage)
        }


    }

    private fun setObservers(adapter: InventoryItemAdapter) {
        //Observer für das Navigieren zum AddProduct-Screen
        inventoryViewModel.navigateToAddProduct.observe(this, Observer { prodId ->
            prodId?.let {
                this.findNavController()
                    .navigate(InventoryFragmentDirections.inventoryDestinationToAddProductDestination(prodId))
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
        inventoryViewModel.inventoryItems.observe(this, Observer {
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