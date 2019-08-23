package de.jl.groceriesmanager.inventory

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.dialog.product.ProductDialogFragment


class InventoryFragment : Fragment() {

    lateinit var inventoryBinding: de.jl.groceriesmanager.databinding.FragmentInventoryBinding
    lateinit var application: Application
    lateinit var viewModelFactory: GroceriesManagerViewModelFactory
    lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            (activity as AppCompatActivity).supportActionBar?.title =
                getString(de.jl.groceriesmanager.R.string.common_inventory)
            //Binding
            inventoryBinding =
                DataBindingUtil.inflate(inflater, de.jl.groceriesmanager.R.layout.fragment_inventory, container, false)

            //Application
            application = requireNotNull(this.activity).application

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

            val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
            itemDecorator.setDrawable(ContextCompat.getDrawable(this.context!!, R.drawable.rv_devider)!!)

            inventoryBinding.inventoryItemList.addItemDecoration(itemDecorator)
            inventoryBinding.inventoryItemList.layoutManager = GridLayoutManager(activity, 1)
            inventoryBinding.insertNewProductBtn.setOnClickListener { navigateToProductDialog(Pair(0L, Pair(0L, ""))) }
            setObservers(adapter)
            validateArguments()

            return inventoryBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(de.jl.groceriesmanager.R.layout.fragment_inventory, container, false)
    }

    private fun validateArguments() {
        try {
            val args: InventoryFragmentArgs by navArgs()
            val productId = args.productId
            val expiryDateString = args.expiryDateString
            if (productId > 0 && !expiryDateString.isNullOrEmpty()) {
                inventoryViewModel.newProductInserted(Pair(0L, Pair(productId, expiryDateString)))
            }

        } catch (e: Exception) {
            Log.e("InventoryFragment", "Failed to validate Args: " + e.localizedMessage)
        }
    }

    private fun setObservers(adapter: InventoryItemAdapter) {
        //Observer für das Navigieren zum AddProduct-Screen
        inventoryViewModel.navigateToAddProduct.observe(this, Observer { pair ->
            pair?.let {
                navigateToProductDialog(pair)
                inventoryViewModel.doneNavigatingToAddProduct()
            }
        })

        //Observer für Hinzufügen eines neuen InventoryItems
        inventoryViewModel.newProductInventoryItem.observe(this, Observer { value ->
            value?.let {
                inventoryViewModel.addNewProductItem()
                inventoryViewModel.doneAddNewProductItem()
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

    private fun navigateToProductDialog(pair: Pair<Long, Pair<Long, String>>) {
        val dialog = ProductDialogFragment(pair.first, pair.second.first, pair.second.second)
        fragmentManager?.let {
            dialog.setTargetFragment(this, 0)
            dialog.show(it, "Product Dialog")

        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                inventoryViewModel.deleteInventoryItem(item.groupId.toLong())
            }
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (data != null) {
                val values = data.extras
                if (values != null) {
                    if (values.containsKey("ProdId") && values.containsKey("ExpDate")) {
                        val invId: Long = if (values.containsKey("InvId")) {
                            values.getLong("InvId")
                        } else {
                            0L
                        }
                        val prodId = values.getLong("ProdId")
                        val expDate = values.getString("ExpDate")
                        inventoryViewModel.newProductInserted(Pair(invId, Pair(prodId, expDate)))
                    }
                }
            }
        }
    }
}