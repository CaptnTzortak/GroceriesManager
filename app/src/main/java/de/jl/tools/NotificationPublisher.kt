package de.jl.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.GroceriesManagerRepository


class NotificationPublisher : BroadcastReceiver() {

    companion object {
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show()
        NotifyAsyncTask().execute(context)
    }

    class NotifyAsyncTask : AsyncTask<Context, Void, String>() {
        override fun doInBackground(vararg params: Context?): String? {
            val channelID = "de.jl.groceriesmanager.notifications.expirydates"

            val context = params[0]
            if (context != null) {
                //val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                //Repo:
                val repo = GroceriesManagerRepository(context)
                val expiredItems = repo.inventorysWithExpiryDateCloserOne
                var counter = 0
                expiredItems?.iterator()?.forEach {

                    var builder = NotificationCompat.Builder(context, channelID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("${it.product.description} expiring")
                        .setContentText("${it.product.description} expires at ${it.expiryDateString}")
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText("${it.product.description} expires at ${it.expiryDateString}")
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    if (builder != null) {
                        NotificationManagerCompat.from(context).notify(counter, builder.build())
                        counter++
                    }
                }
            }
            Log.i("test", "Notify async started")
            return ""
        }
    }

}