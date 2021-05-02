package com.example.projectsingularity

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

@RequiresApi(api = Build.VERSION_CODES.O)

open class MainActivity : AppCompatActivity() {
    var fm = supportFragmentManager
    var fragment: Fragment = PolarHrFragment()
    var fragment1: Fragment = FileIoFragment()
    var added = false

    companion object {
        private const val LOCATION_ACCESS_REQUEST = 1
        var contextOfApplication: Context? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        contextOfApplication = applicationContext

        //FrameLayout framelay = (FrameLayout) findViewById(R.id.framelay);
        val tabLayout = findViewById<View>(R.id.tablay) as TabLayout
        val firstTab = tabLayout.newTab()
        firstTab.text = "Connect" // set the Text for the first Tab
        //firstTab.setIcon(R.drawable.ic_launcher_foreground); // set an icon for the
        // first tab
        tabLayout.addTab(firstTab) // add  the tab at in the TabLayout

        // Create a new Tab named "Second"
        val secondTab = tabLayout.newTab()
        secondTab.text = "Record" // set the Text for the second Tab
        //secondTab.setIcon(R.drawable.ic_launcher_foreground); // set an icon for the second tab
        tabLayout.addTab(secondTab) // add  the tab  in the TabLayout
        val ft = fm.beginTransaction()
        if (!added) {
            ft.add(R.id.framelay, fragment)
            ft.add(R.id.framelay, fragment1)
            ft.hide(fragment)
            ft.hide(fragment1)
            added = true
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.show(fragment)
        ft.hide(fragment1)
        ft.commit()
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val ft = fm.beginTransaction()
                when (tab.position) {
                    0 -> {
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        ft.show(fragment)
                        ft.hide(fragment1)
                        ft.commit()
                    }
                    1 -> {
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        ft.hide(fragment)
                        ft.show(fragment1)
                        ft.commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && savedInstanceState == null) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_ACCESS_REQUEST
            )
        }
    }

}