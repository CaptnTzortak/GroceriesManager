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
            itemDecorator.setDrawable(ContextCompat.getDrawable(this!!.context!!, R.drawable.rv_devider)!!)

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
                newProductInserted()
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

    private fun newProductInserted() {
        //val channelID = "de.jl.groceriesmanager.notifications.expirydates"
//
        ////if (context != null) {
////
        ////    val notManager = application.getSystemService(NOTIFICATION_SERVICE)
        ////    val notificationBuilder = NotificationCompat.Builder(context!!, channelID)
////
        ////    notificationBuilder.setAutoCancel(true)
        ////        .setColor(ContextCompat.getColor(context!!, R.color.primaryColor))
        ////        .setContentTitle(getString(R.string.app_name))
        ////        .setContentText("")
        ////        .setDefaults(Notification.DEFAULT_ALL)
        ////        .setWhen(System.currentTimeMillis())
        ////        .setSmallIcon(R.drawable.ic_launcher_background)
        ////        .setAutoCancel(true)
////
        ////    val x = notificationBuilder.build()
        ////}
//
//
        //var builder = context?.let {
        //    NotificationCompat.Builder(it, channelID)
        //        .setSmallIcon(R.mipmap.ic_launcher)
        //        .setContentTitle("My notification")
        //        .setContentText("Much longer text that cannot fit one line...")
        //        .setStyle(
        //            NotificationCompat.BigTextStyle()
        //                .bigText("Much longer text that cannot fit one line...")
        //        )
        //        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        //}
//
        //if (builder != null) {
        //    //Notification.Builder.recoverBuilder(context, builder.build()).createContentView()
        //    this.context?.let { NotificationManagerCompat.from(it) }?.notify(0, builder.build())
//
        //}

// notificationId is a unique int for each notification that you must define



        //  if (builder != null) {
        //      with(context?.let { NotificationManagerCompat.from(it) }) {
        //          // notificationId is a unique int for each notification that you must define
        //          this?.notify(0, builder.build())
        //      }
        //  }


        // val not = activity!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // val notIntent = Intent()
        // val conIntent = PendingIntent.getActivities(activity,0, notIntent, 0)
        // val notification = Notification(    )
        // notification.setLatestEventInfo(context, contextTitle, contentText, contentIntent);
        // not.notify(1, notification);

        // val notificationID = 101


        // val notification = Notification.Builder(context, channelID)
        //     .setContentTitle("Example Notification")
        //     .setContentText("This is an  example notification.")
        //     .setSmallIcon(android.R.drawable.ic_dialog_info)
        //     .setChannelId(channelID)
        //     .build()

        // val mng = activity?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // mng?.notify(notificationID, notification)

    }

    private fun navigateToProductDialog(pair: Pair<Long, Pair<Long,String>>) {
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
                if (data.extras.containsKey("ProdId") && data.extras.containsKey("ExpDate")) {
                    val invId: Long = if(data.extras.containsKey("InvId")){
                        data.extras.getLong("InvId")
                    } else {
                        0L
                    }
                    val prodId = data.extras.getLong("ProdId")
                    val expDate = data.extras.getString("ExpDate")
                    inventoryViewModel.newProductInserted(Pair(invId, Pair(prodId, expDate)))
                }
            }
        }
    }
}