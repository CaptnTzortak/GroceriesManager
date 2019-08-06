package de.jl.groceriesmanager.grocery_lists

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.FragmentGroceryListsBinding
import de.jl.tools.openDialogNewGroceryList

class GroceryListsFragment : Fragment() {

    private lateinit var groceryListsViewModel: GroceryListsViewModel
    private lateinit var viewModelFactory: GroceriesManagerViewModelFactory
    private lateinit var groceryListsBinding: FragmentGroceryListsBinding
    private lateinit var application: Application

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.common_grocery_lists)

            //Binding
            groceryListsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_grocery_lists, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //ViewModelFactory
            viewModelFactory = GroceriesManagerViewModelFactory(application)

            //ViewModel
            groceryListsViewModel = ViewModelProviders.of(this, viewModelFactory).get(GroceryListsViewModel::class.java)

            //Adapter für RecyclerView
            val adapter = GroceryListsItemAdapter(GroceryListsItemListener {
                groceryListsViewModel.openGroceryList(it)
            })

            groceryListsBinding.lifecycleOwner = this
            groceryListsBinding.viewModel = groceryListsViewModel
            groceryListsBinding.groceryListsList.adapter = adapter
            groceryListsBinding.groceryListsList.layoutManager = GridLayoutManager(activity, 1)
            groceryListsBinding.addBtn.setOnClickListener { openAddGroceryListDialog() }

            setObservers(adapter)
            //validateArguments
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
               // addNewGroceryList()
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

        groceryListsViewModel.openGroceryList.observe(this, Observer{
            it?.let{
                this.findNavController()
                    .navigate(GroceryListsFragmentDirections.actionGroceryListsDestinationToGroceryListFragment(it, ""))
                groceryListsViewModel.doneOpenGroceryList()
            }
        })
    }

    private fun openAddGroceryListDialog() {
        context?.let {
            openDialogNewGroceryList(context!!, View.OnClickListener{
                //TODO: Ermitteln des Textes
                //groceryListsViewModel.newGroceryList(editText.text.toString())
            })
            //val db = AlertDialog.Builder(context!!)
            //val layoutView = layoutInflater.inflate(R.layout.dialog_new_product, null)
            //db.setView(layoutView)
            //val dialog = db.create()
            //dialog.window.attributes.windowAnimations = R.style.AppTheme_DialogAnimation
            //dialog.show()
            //dialog.window.setBackgroundDrawable(ColorDrawable(0))
        }//
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