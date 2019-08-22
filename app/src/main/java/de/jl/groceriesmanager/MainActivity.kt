package de.jl.groceriesmanager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import de.jl.groceriesmanager.databinding.ActivityMainBinding
import de.jl.tools.NotificationPublisher
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //DataBinding
    private lateinit var mainActivityBinding: ActivityMainBinding

    companion object {
        private const val NOTIFICATION_CHANNEL_BASE_ID = "de.jl.groceriesmanager.notifications"
        const val EXPIRY_DATES_NOTIFICATION_CHANNEL_ID = "$NOTIFICATION_CHANNEL_BASE_ID.expirydates"

        const val SHARED_PREFERENCES_ID = "Groceries_Manager_Preferences"


        const val TAG = "MainActivity"
        const val EXPIRY_DATE_NOTIFICATION_AT_HOUR = 16
        const val EXPIRY_DATE_NOTIFICATION_AT_MINUTE = 30
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)

            //Databinding
            mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

            //Navigation
            val host: NavHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
            val navController = host.navController
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val dest: String = try {
                    resources.getResourceName(destination.id)
                } catch (e: Resources.NotFoundException) {
                    Integer.toString(destination.id)
                }
                Log.d("NavigationActivity", "Navigated to $dest")
            }
            setupBottomNavMenu(navController)

            //Toolbar
            setSupportActionBar(toolbar)

            val preferences = getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE)
            if(!preferences.getBoolean("ExpiryDateNotificationAlarm", false)){
                setupNotificationAlarm()
                Log.i(TAG,"ExpiryDateNotificationAlarm Set")
                val editor = preferences.edit()
                editor.putBoolean("ExpiryDateNotificationAlarm", true)
                editor.commit()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    EXPIRY_DATES_NOTIFICATION_CHANNEL_ID,
                    "GroceriesManager Notifications",
                    "Notificationchannel for expired groceries"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
        }
    }

    private fun setupNotificationAlarm() {
        val intent = Intent(this, NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val firingCal = Calendar.getInstance()
        val currentCal = Calendar.getInstance()

        firingCal.set(Calendar.HOUR_OF_DAY, EXPIRY_DATE_NOTIFICATION_AT_HOUR) // At the hour you wanna fire
        firingCal.set(Calendar.MINUTE, EXPIRY_DATE_NOTIFICATION_AT_MINUTE) // Particular minute
        firingCal.set(Calendar.SECOND, 0) // particular second

        var intendedTime = firingCal.timeInMillis
        val currentTime = currentCal.timeInMillis

        if (intendedTime >= currentTime) {
            // you can add buffer time too here to ignore some small differences in milliseconds
            // set from today
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent)
        } else {
            // set from next day
            // you might consider using calendar.add() for adding one day to the current day
            firingCal.add(Calendar.DAY_OF_MONTH, 1)
            intendedTime = firingCal.timeInMillis

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }

    private fun createNotificationChannel(id: String, name: String, description: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        mainActivityBinding.bottomNavView.setupWithNavController(navController)
    }
}