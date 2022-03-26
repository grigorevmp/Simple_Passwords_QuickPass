package com.mikhailgrigorev.quickpassword.ui.main_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikhailgrigorev.quickpassword.R
import com.mikhailgrigorev.quickpassword.common.utils.Utils
import com.mikhailgrigorev.quickpassword.databinding.ActivityMainBinding
import com.mikhailgrigorev.quickpassword.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.quickpassword.di.modules.RoomModule

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        val navHostFragment: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        navController = navHostFragment.navController
        setUpBottomNav(binding.bottomTabBar)

    }

    private fun setUpBottomNav(bottomNav: BottomNavigationView) {
        navController?.let { navController ->
            bottomNav.setupWithNavController(navController)
            bottomNav.setOnItemReselectedListener {
            }
        }
        bottomNav.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(
                    item,
                    Navigation.findNavController(this, R.id.nav_host_fragment)
            )
        }
    }
}