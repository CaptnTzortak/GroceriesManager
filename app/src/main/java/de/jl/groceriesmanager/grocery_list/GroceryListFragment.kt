package de.jl.groceriesmanager.grocery_list

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.SwipeToSetDoneCallback
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao
import de.jl.groceriesmanager.database.products.ProductsDao

class GroceryListFragment : Fragment() {

    private lateinit var groceryListViewModel: GroceryListViewModel
    private lateinit var viewModelFactory: GroceriesManagerViewModelFactory
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
            glId = args.glId
            prodId = args.prodId
            note = args.note

            //ViewModelFactory
            viewModelFactory = GroceriesManagerViewModelFactory(application, productsDB, null, glDB, glItemMappingDB, glId, prodId)

            //ViewModel
            groceryListViewModel = ViewModelProviders.of(this, viewModelFactory).get(GroceryListViewModel::class.java)

            //Adapter für RecyclerView
            val adapter = GroceryListItemAdapter(GroceryListItemListener {

            })

            groceryListBinding.lifecycleOwner = this
            groceryListBinding.viewModel = groceryListViewModel
            groceryListBinding.groceryListItemList.adapter = adapter
            groceryListBinding.groceryListItemList.layoutManager = GridLayoutManager(activity, 1)

            val swipeHandler = object : SwipeToSetDoneCallback(application) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val vh = viewHolder as GroceryListItemAdapter.ViewHolder
                    groceryListViewModel.flipDoneForGLItemMapping(vh.getItem())
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(groceryListBinding.groceryListItemList)

            setObservers(adapter)
            validateArguments(args)

            return groceryListBinding.root
        } catch (e: Exception) {
            Log.d("GroceryListFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grocery_list, container, false)
    }


    private fun validateArguments(args: GroceryListFragmentArgs) {
        try {
            val productId = args.prodId
            //val noteVal = args.note
            if (productId > 0) {
                groceryListViewModel.newProductInserted(productId)
            }

        } catch (e: Exception) {
            Log.e("InventoryFragment", "Failed to validate Args: " + e.localizedMessage)
        }
    }

    private fun setObservers(adapter: GroceryListItemAdapter) {
        //Observer für Hinzufügen eines neuen GroceryListItems
        groceryListViewModel.newProductGroceryListItem.observe(this, Observer { value ->
            value?.let {
                groceryListViewModel.addNewProductItem()
            }
        })

        groceryListViewModel.glItemMappingList.observe(this, Observer { it ->
            it?.let {
                adapter.submitList(it)
            }
        })

        groceryListViewModel.addProduct.observe(this, Observer { it ->
            it?.let {
                this.findNavController()
                    .navigate(
                        GroceryListFragmentDirections.groceryListDestinationToAddProductGroceryListDestination(
                            it,
                            glId
                        )
                    )
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