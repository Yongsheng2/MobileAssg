package com.tarc.edu.etrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tarc.edu.etrack.ui.favorite.FavoriteFragment
import com.tarc.edu.etrack.ui.find_station.FindStationFragment
import com.tarc.edu.etrack.ui.home.HomeFragment
import com.tarc.edu.etrack.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private val bottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.bottom_navigation)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
        true

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.menu_find_station -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FindStationFragment())
                        .commit()
                    true
                }
                R.id.menu_favorite -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FavoriteFragment())
                        .commit()
                    true
                }
                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }



    }



}




