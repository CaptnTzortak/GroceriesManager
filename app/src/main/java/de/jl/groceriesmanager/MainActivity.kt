package de.jl.groceriesmanager

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityBinding: de.jl.groceriesmanager.databinding.ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mainActivityBinding.toolbar)
        val host: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        val navController = host.navController
        setupBottomNavMenu(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                Integer.toString(destination.id)
            }
            Log.d("NavigationActivity", "Navigated to $dest")
        }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        mainActivityBinding.bottomNavView.setupWithNavController(navController)
    }
}