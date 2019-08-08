package de.jl.groceriesmanager

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.app.AlarmManager




class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityBinding: de.jl.groceriesmanager.databinding.ActivityMainBinding
    private var pendingIntent: PendingIntent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val host: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        val navController = host.navController
        setupBottomNavMenu(navController)
        setSupportActionBar(toolbar)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                Integer.toString(destination.id)
            }
            Log.d("NavigationActivity", "Navigated to $dest")
        }

        //createNotificationChannel(
        //    "de.jl.groceriesmanager.notifications.expirydates",
        //    "GroceriesManager ExpiryDates",
        //    "ExpiryDates-Notifications for inventory products"
        //)

        //  setupExpiryDateNotifications()
        //  scheduleNotification(getNotification("5 second delay"), 5000)




        val sharedpreferences = getSharedPreferences("GroceriesManagerPreferences", Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()

        if (!sharedpreferences.getBoolean("alarm", false)) {
            setupNotificationAlarm()

           ///* Retrieve a PendingIntent that will perform a broadcast */
           //val alarmIntent = Intent(this, NotificationPublisher::class.java)
           //val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)
           //val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
           //val interval = 8000L

           ///* Set the alarm to start at 10:30 AM */
           //val calendar = Calendar.getInstance()
           //calendar.timeInMillis = System.currentTimeMillis()
           //calendar.set(Calendar.HOUR_OF_DAY, 20)
           //calendar.set(Calendar.MINUTE, 1)

           ///* Repeating on every 24 hours interval */
           //manager.setRepeating(
           //    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
           //    (1000 * 60 * 60 * 24).toLong(), pendingIntent
           //)
            Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show()
            editor.putBoolean("alarm", true)
            editor.commit()
        }
    }

    private fun setupNotificationAlarm() {
        val intent = Intent(this, NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val firingCal = Calendar.getInstance()
        val currentCal = Calendar.getInstance()

        firingCal.set(Calendar. HOUR_OF_DAY,10) // At the hour you wanna fire
        firingCal.set(Calendar.MINUTE, 30) // Particular minute
        firingCal.set(Calendar.SECOND, 0) // particular second


        var intendedTime = firingCal.timeInMillis
        val currentTime = currentCal.timeInMillis

        if(intendedTime >= currentTime){
            // you can add buffer time too here to ignore some small differences in milliseconds
            // set from today
            alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent)
        } else{
            // set from next day
            // you might consider using calendar.add() for adding one day to the current day
            firingCal.add(Calendar.DAY_OF_MONTH, 1)
            intendedTime = firingCal.timeInMillis

            alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }

    private fun createNotificationChannel(
        id: String, name: String,
        description: String
    ) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = name
            val description = description
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupExpiryDateNotifications() {
        // /* Retrieve a PendingIntent that will perform a broadcast */
        // val alarmIntent = Intent(this, NotificationPublisher::class.java)
        // pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)

        // val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // /* Set the alarm to start at 10:30 AM */
        // val calendar = Calendar.getInstance()
        // calendar.timeInMillis = System.currentTimeMillis()
        // calendar.set(Calendar.HOUR_OF_DAY, 17)
        // calendar.set(Calendar.MINUTE, 42)

        // /* Repeating on every 24 hours interval */
        // manager.setRepeating(
        //     AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
        //     (1000 * 60 * 60 * 24).toLong(), pendingIntent
        // )
    }

    private fun setupBottomNavMenu(navController: NavController) {
        mainActivityBinding.bottomNavView.setupWithNavController(navController)
    }


    private fun scheduleNotification(notification: Notification, delay: Int) {

        val notificationIntent = Intent(this, NotificationPublisher::class.java)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val futureInMillis = SystemClock.elapsedRealtime() + delay
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
    }

    // private fun getNotification(content: String): Notification {
    //     val builder = Notification.Builder(this)
    //     builder.setContentTitle("Scheduled Notification")
    //     builder.setContentText(content)
    //     builder.setSmallIcon(R.mipmap.ic_launcher)
    //     return builder.build()
    // }


}