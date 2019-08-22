package de.jl.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.jl.groceriesmanager.MainActivity
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerRepository
import de.jl.groceriesmanager.database.inventory.Inventory


class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotifyAsyncTask().execute(context)
    }

    class NotifyAsyncTask : AsyncTask<Context, Void, String>() {
        override fun doInBackground(vararg params: Context?): String? {
            val context = params[0]
            if (context != null) {
                val repo = GroceriesManagerRepository(context)
                val expiredItems = repo.inventorysWithExpiryDateCloserOne
                var counter = 0
                expiredItems.iterator().forEach {
                    showExpiryDateNotification(it, context, counter)
                    counter++
                }
            }
            Log.i("test", "Notify async started")
            return ""
        }

        private fun showExpiryDateNotification(
            invItem: Inventory,
            context: Context,
            counter: Int
        ) {
            val prod = invItem.product
            if (prod != null) {
                val builder = NotificationCompat.Builder(context, MainActivity.EXPIRY_DATES_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(
                        context.getString(R.string.product_expires).replace("{0}", prod.name)
                    )
                    .setContentText(
                        context.getString(R.string.text_product_expires).replace(
                            "{0}",
                            prod.name
                        ).replace("{1}", invItem.expiryDateString)
                    )
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(
                                context.getString(R.string.text_product_expires).replace(
                                    "{0}",
                                    prod.name
                                ).replace("{1}", invItem.expiryDateString)
                            )
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                if (builder != null) {
                    NotificationManagerCompat.from(context).notify(counter, builder.build())
                }
            }
        }
    }

}