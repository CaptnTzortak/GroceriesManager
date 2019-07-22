package de.jl.groceriesmanager.grocery_lists

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerDB
import de.jl.groceriesmanager.database.groceryList.GroceryListsDao
import de.jl.groceriesmanager.databinding.FragmentGroceryListsBinding

class GroceryListsFragment : Fragment() {

    private lateinit var groceryListsViewModel: GroceryListsViewModel
    private lateinit var viewModelFactory: GroceryListsViewModelFactory
    private lateinit var groceryListsBinding: FragmentGroceryListsBinding
    private lateinit var application: Application
    private lateinit var dataSource: GroceryListsDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            //Binding
            groceryListsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_grocery_lists, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //DataSource
            dataSource = GroceriesManagerDB.getInstance(application).groceryListsDao

            //ViewModelFactory
            viewModelFactory = GroceryListsViewModelFactory(dataSource, application)

            //ViewModel
            groceryListsViewModel = ViewModelProviders.of(this, viewModelFactory).get(GroceryListsViewModel::class.java)

            //Adapter für RecyclerView
            val adapter = GroceryListsItemAdapter(GroceryListsItemListener {
                //groceryListsViewModel.openGroceryList(glId)
            })

            groceryListsBinding.lifecycleOwner = this
            groceryListsBinding.viewModel = groceryListsViewModel
            groceryListsBinding.groceryListsList.adapter = adapter
            groceryListsBinding.groceryListsList.layoutManager = GridLayoutManager(activity, 1)
            groceryListsBinding.addBtn.setOnClickListener { addNewGroceryList() }

            setObservers(adapter)
            //validateArguments()

            return groceryListsBinding.root
        } catch (e: Exception) {
            Log.d("GroceryListFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grocery_lists, container, false)
    }

    private fun setObservers(adapter: GroceryListsItemAdapter) {
        groceryListsViewModel.newGroceryList.observe(this, Observer {
            it?.let {
                addNewGroceryList()
            }
        })

        //Observer für Recycler-View Items
        groceryListsViewModel.groceryLists.observe(this, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        groceryListsViewModel.newGroceryListDescription.observe(this, Observer {
            it?.let {
                groceryListsViewModel.insertNewGroceryList()
            }
        })

    }

    fun addNewGroceryList() {
        try {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Insert Grocery List Description")

            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.grocery_list_description_dialog, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editText)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Confirm") { dialog, id ->
                //TODO: Add New Grocery List
                groceryListsViewModel.newGroceryList(editText.text.toString())
            }
            builder.setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }

            builder.show()
        } catch (e: Exception) {
            Log.d("GroceryListsFragment", e.localizedMessage)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                groceryListsViewModel.deleteGroceryList(item.groupId.toLong())
            }
        }
        return true
    }
}