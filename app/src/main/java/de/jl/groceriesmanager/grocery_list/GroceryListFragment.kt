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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.inventory.GLItemMappingDao

class GroceryListFragment : Fragment() {

    private lateinit var groceryListViewModel: GroceryListViewModel
    private lateinit var viewModelFactory: GroceryListViewModelFactory
    private lateinit var groceryListBinding: de.jl.groceriesmanager.databinding.FragmentGroceryListBinding
    private lateinit var application: Application
    private lateinit var dataSource: GLItemMappingDao

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
            dataSource = GroceriesManagerDB.getInstance(application).glItemMappingDao

            //ViewModelFactory
            viewModelFactory = GroceryListViewModelFactory(dataSource, application)

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

            setObservers(adapter)
            validateArguments()

            return groceryListBinding.root
        } catch (e: Exception) {
            Log.d("GroceryListFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grocery_list, container, false)
    }

    private fun addNewProduct() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setObservers(adapter: GroceryListItemAdapter) {
        groceryListViewModel.glId.observe(this, Observer {
            it?.let {
                groceryListViewModel.reloadData()
            }
        })

        groceryListViewModel.glItemMappingList.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        groceryListViewModel.groceryList.observe(this, Observer{
            it?.let {
                groceryListViewModel.refreshGroceryListDetails()
            }
        })
    }

    private fun validateArguments() {
        try {
            val args: GroceryListFragmentArgs by navArgs()
            val glId = args.glId
            if (glId > 0) {
                groceryListViewModel.setGlId(glId)
            }
        } catch (e: Exception) {
            Log.e("InventoryFragment", "Failed to validate Args: " + e.localizedMessage)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                //groceryListViewModel.deleteGroceryList(item.groupId.toLong())
            }
        }
        return true
    }
}