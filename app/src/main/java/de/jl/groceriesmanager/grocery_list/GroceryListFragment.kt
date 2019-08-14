package de.jl.groceriesmanager.grocery_list

import android.app.Application
import android.app.DatePickerDialog
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.tools.SwipeToSetDoneCallback
import de.jl.groceriesmanager.database.groceryListsProducts.GroceryListsProducts
import de.jl.groceriesmanager.dialog.product.ProductDialogFragment
import de.jl.tools.openDatePicker

class GroceryListFragment : Fragment() {

    private lateinit var groceryListViewModel: GroceryListViewModel
    private lateinit var viewModelFactory: GroceriesManagerViewModelFactory
    private lateinit var groceryListBinding: de.jl.groceriesmanager.databinding.FragmentGroceryListBinding
    private lateinit var application: Application
    private var glId = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {

            //Binding
            groceryListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_grocery_list, container, false)

            //Application
            application = requireNotNull(this.activity).application

            val args: GroceryListFragmentArgs by navArgs()
            glId = args.glId

            //ViewModelFactory
            viewModelFactory = GroceriesManagerViewModelFactory(application, 0L, "", glId)

            //ViewModel
            groceryListViewModel = ViewModelProviders.of(this, viewModelFactory).get(GroceryListViewModel::class.java)

            //Adapter für RecyclerView
            val adapter = GroceryListItemAdapter(GroceryListItemListener {
                groceryListViewModel.modifyProduct(it)
            })

            groceryListBinding.lifecycleOwner = this
            groceryListBinding.viewModel = groceryListViewModel
            groceryListBinding.groceryListItemList.adapter = adapter
            val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
            itemDecorator.setDrawable(ContextCompat.getDrawable(this.context!!, R.drawable.rv_devider)!!)

            groceryListBinding.groceryListItemList.addItemDecoration(itemDecorator)

            groceryListBinding.groceryListItemList.layoutManager = GridLayoutManager(activity, 1)

            val swipeHandler = object : SwipeToSetDoneCallback(application) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val vh = viewHolder as GroceryListItemAdapter.ViewHolder
                    groceryListViewModel.flipDoneForGLItemMapping(vh.getItem())
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(groceryListBinding.groceryListItemList)

            groceryListBinding.insertNewProductBtn.setOnClickListener { onInsertNewProductClicked() }

            setObservers(adapter)
            validateArguments(args)

            return groceryListBinding.root
        } catch (e: Exception) {
            Log.d("GroceryListFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grocery_list, container, false)
    }

    private fun onInsertNewProductClicked() {
        navigateToProductDialog(Pair(0L,""))
    }

    private fun navigateToProductDialog(pair: Pair<Long, String>, quantity: Int = 1) {
        val dialog =
            ProductDialogFragment(pair.first, null, pair.second, quantity)
        fragmentManager?.let {
            dialog.setTargetFragment(this, 0)
            dialog.show(it, "Product Dialog")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (data != null) {
                if (data.extras.containsKey("ProdId") && data.extras.containsKey("Note")) {
                    val prodId = data.extras.getLong("ProdId")
                    val note = data.extras.getString("Note")
                    val quantity = data.extras.getInt("Quantity")
                    groceryListViewModel.newProductInserted(GroceryListsProducts(0L,prodId, 0L, note, quantity))
                }
            }
        }
    }

    private fun validateArguments(args: GroceryListFragmentArgs) {
        try {
            val productId = args.prodId
            var note = args.note
            if (productId > 0) {
                groceryListViewModel.newProductInserted(GroceryListsProducts(0L,productId, 0L, note, 1))
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

        groceryListViewModel.groceryListProducts.observe(this, Observer {
            groceryListViewModel.fillGroceryListProducts()

        })

        groceryListViewModel.filledGroceryListProducts.observe(this, Observer { it ->
            it?.let {
                adapter.submitList(it)
            }
        })

        groceryListViewModel.addProduct.observe(this, Observer { it ->
            it?.let {
                navigateToProductDialog(Pair(it.prodId, it.note), it.quantity)
                groceryListViewModel.doneNavigatingToAddProductGL()
            }
        })

        groceryListViewModel.reset.observe(this, Observer {
            it?.let {
                groceryListViewModel.resetGLProducts(it)
            }
        })

        groceryListViewModel.glName.observe(this, Observer{ glName ->
            glName?.let {
                (activity as AppCompatActivity).supportActionBar?.title = it

            }
        })
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            121 -> {
                groceryListViewModel.deleteGroceryListsProducts(item.groupId.toLong())
            }
            122 -> {
                context?.let {
                    openDatePicker(it, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        var rMonth = month +1
                        if(rMonth == 13){
                            rMonth = 1
                        }
                        val realMonth = if(rMonth < 10){
                            "0$rMonth"
                        } else{
                            "$rMonth"
                        }
                        val expiryDateString = """$dayOfMonth.$realMonth.$year"""
                        groceryListViewModel.addProductToInventory(item.groupId.toLong(), expiryDateString)

                    })
                }
            }
        }
        return true
    }
}