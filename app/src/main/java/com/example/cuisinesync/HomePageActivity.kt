package com.example.cuisinesync

import MapFragment
import ProfileFragment
import SettingsFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.cuisinesync.databinding.ActivityHomePageBinding

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomePageBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(R.layout.activity_home_page)

        val mapFragment=MapFragment()
        val profileFragment=ProfileFragment()
        val settingsFragment=SettingsFragment()

        setCurrentFragment(mapFragment)


        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home->setCurrentFragment(mapFragment)
                R.id.person->setCurrentFragment(profileFragment)
                R.id.settings->setCurrentFragment(settingsFragment)

            }
            true
        }

    }

    private fun setCurrentFragment(fragment:Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }
}