package de.jl.groceriesmanager

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationPublisher: BroadcastReceiver(){

    companion object{
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val channelID = "de.jl.groceriesmanager.notifications.expirydates"

        Toast.makeText(context,"I'm running", Toast.LENGTH_SHORT).show()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var builder = context?.let {
            NotificationCompat.Builder(it, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line...")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        if (builder != null) {
            //Notification.Builder.recoverBuilder(context, builder.build()).createContentView()
            context.let { NotificationManagerCompat.from(it) }.notify(0, builder.build())
        }
        NotifyAsyncTask().execute(notificationManager)
    }

    class NotifyAsyncTask : AsyncTask<NotificationManager, Void, String>() {
        override fun doInBackground(vararg params: NotificationManager?): String? {
            Log.i("test", "Notify async started")
            //invRepository = InventoryRepository(context)
            //val list = invRepository.getAllExpiredProducts()
            //TODO Iterate and Notify
            return ""
        }
    }

    //private static class notifyAsyncTask extends AsyncTask<Context, Void, Void> {

   //    private String CHANNEL_ID = "1a2b3c4d";

   //    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

   //    @Override
   //    protected Void doInBackground(Context... params) {
   //        Log.i(TAG, "Notify async started");
   //        ReminderRepository mRepository; = new ReminderRepository(context);
   //        List<Reminder> list = mRepository.getAllReminder();
   //        for(Reminder r : list) {
   //        // TODO
   //    }
   //    }
   //}
}