package de.jl.groceriesmanager.grocerylist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.GroceryListFragmentBinding

class GroceryListFragment : Fragment() {

    private lateinit var groceryListViewModel: GroceryListViewModel
    private lateinit var groceryListViewModelFactory: GroceryListViewModelFactory
    private lateinit var groceryListBinding: GroceryListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        try{
            //Binding
            groceryListBinding = DataBindingUtil.inflate(inflater, R.layout.grocery_list_fragment, container, false)

            //ViewModelFactory
            //mit Parameter:
            //groceryListViewModelFactory = GroceryListViewModelFactory(GroceriyListFragmentArgs.fromBundle(arguments!!).score)
            //Ohne Parameter:
            groceryListViewModelFactory = GroceryListViewModelFactory()
            //ViewModel
            groceryListViewModel = ViewModelProviders.of(this, groceryListViewModelFactory).get(GroceryListViewModel::class.java)

            return groceryListBinding.root

        } catch(e: Exception){
            Log.d("InventoryFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.grocery_list_fragment, container, false)
    }
}