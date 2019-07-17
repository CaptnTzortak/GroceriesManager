package de.jl.groceriesmanager.product.add

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
import de.jl.groceriesmanager.database.products.ProductItem
import de.jl.groceriesmanager.database.products.ProductsDao
import de.jl.groceriesmanager.inventory.InventoryFragmentDirections
import de.jl.groceriesmanager.inventory.InventoryViewModel
import de.jl.groceriesmanager.inventory.InventoryViewModelFactory

class AddProductFragment : Fragment() {

    lateinit var addProductBinding: de.jl.groceriesmanager.databinding.FragmentAddProductBinding
    lateinit var application: Application
    lateinit var dataSource: ProductsDao
    lateinit var viewModelFactory: AddProductViewModelFactory
    lateinit var addProductViewModel: AddProductViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        try{
            //Binding
            addProductBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_product, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //TODO: Adapter:
            //val adapter = ItemAdapter()

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).productsDao

            //ViewModelFactory
            viewModelFactory = AddProductViewModelFactory(dataSource, application)

            addProductViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddProductViewModel::class.java)
            //Observing
            //TODO uncomment after adapter
            //inventoryViewModel.inventoryItems.observe(viewLifecycleOwner, Observer {
            //    it?.let {
            //        adapter.submitList(it)
            //    }
            //})

            addProductViewModel.navigateToInventory.observe(this, Observer { productItem->
                productItem?.let{
                    this.findNavController().navigate(AddProductFragmentDirections.actionAddProductDestinationToInventoryDestination(productItem.product_id))
                    addProductViewModel.doneNavigatingToInventory()
                }
            })
            addProductViewModel.product.observe(this, Observer { productItem ->
                productItem?.let {
                    addProductViewModel.productItemChanged()
                }
            })

            addProductBinding.productItem = addProductViewModel.product.value
            addProductBinding.lifecycleOwner = this
            addProductBinding.addProductViewModel = addProductViewModel
            //addProductBinding.inventoryItemList.layoutManager = GridLayoutManager(activity,1 )

            return addProductBinding.root
        } catch (e: Exception) {
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

}