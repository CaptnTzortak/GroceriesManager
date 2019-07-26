package de.jl.groceriesmanager.grocery_list

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import de.jl.groceriesmanager.database.products.ProductsDao

class GroceryListFragment : Fragment() {

    private lateinit var groceryListViewModel: GroceryListViewModel
    private lateinit var viewModelFactory: GroceryListViewModelFactory
    private lateinit var groceryListBinding: de.jl.groceriesmanager.databinding.FragmentGroceryListBinding
    private lateinit var application: Application
    private lateinit var glItemMappingDB: GLItemMappingDao
    private lateinit var glDB: GroceryListsDao
    private lateinit var productsDB: ProductsDao

    var glId = 0L
    var prodId = 0L
    var note = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            //Binding
            groceryListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_grocery_list, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //DataSource
            glItemMappingDB = GroceriesManagerDB.getInstance(application).glItemMappingDao
            glDB = GroceriesManagerDB.getInstance(application).groceryListsDao
            productsDB = GroceriesManagerDB.getInstance(application).productsDao

            val args: GroceryListFragmentArgs by navArgs()

            if(args != null){
                glId = args.glId
            }

            //ViewModelFactory
            viewModelFactory = GroceryListViewModelFactory(glItemMappingDB,glDB, productsDB, application, glId)

            //ViewModel
            groceryListViewModel = ViewModelProviders.of(this, viewModelFactory).get(GroceryListViewModel::class.java)

            //Adapter für RecyclerView
            val adapter = GroceryListItemAdapter(GroceryListItemListener {

            })

            groceryListBinding.lifecycleOwner = this
            groceryListBinding.viewModel = groceryListViewModel
            groceryListBinding.groceryListItemList.adapter = adapter
            groceryListBinding.groceryListItemList.layoutManager = GridLayoutManager(activity, 1)
            groceryListBinding.addBtn.setOnClickListener { addNewProduct() }
            validateArguments(args)
            setObservers(adapter)

            return groceryListBinding.root
        } catch (e: Exception) {
            Log.d("GroceryListFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grocery_list, container, false)
    }

    private fun validateArguments(args: GroceryListFragmentArgs) {
        try {
            prodId = args.prodId
            note = args.note
            if (prodId> 0 && !note.isNullOrEmpty()) {
                groceryListViewModel.newProductInserted(prodId, note)
            }
        } catch (e: Exception) {
            Log.e("InventoryFragment", "Failed to validate Args: " + e.localizedMessage)
        }
    }

    private fun addNewProduct() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setObservers(adapter: GroceryListItemAdapter) {
        groceryListViewModel.glItemMappingList.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        groceryListViewModel.addProduct.observe(this, Observer {
            it?.let{
                this.findNavController()
                    .navigate(GroceryListFragmentDirections.groceryListDestinationToAddProductGroceryListDestination(it, glId))
                groceryListViewModel.doneNavigatingToAddProductGL()
            }
        })
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                groceryListViewModel.deleteGroceryListEntry(item.groupId.toLong())
            }
        }
        return true
    }
}