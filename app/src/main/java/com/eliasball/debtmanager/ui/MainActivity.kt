package com.eliasball.debtmanager.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.databinding.ActivityMainBinding
import com.eliasball.debtmanager.ui.adapters.ViewPagerAdapter
import com.eliasball.debtmanager.ui.fragments.CreateDebtFragment
import com.eliasball.debtmanager.ui.fragments.RateDialog
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // the binding
    private lateinit var binding: ActivityMainBinding

    // page change listener for viewpager
    private lateinit var onPageChangeCallback: ViewPager2.OnPageChangeCallback

    // the shared prefs
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup the viewBinding and set the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // set the top app bar as the support action bar
        setSupportActionBar(binding.topAppBar)

        // create the viewPagerAdapter and link it to the viewPager
        val viewPagerAdapter = ViewPagerAdapter(this)
        binding.pager.adapter = viewPagerAdapter

        // setup the different tabs of the TabLayout
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> "People"
                1 -> "I Owe"
                else -> "I Get"
            }
            tab.icon = when (position) {
                0 -> getDrawable(R.drawable.ic_tab__people_anim)
                1 -> getDrawable(R.drawable.ic_tab_i_owe_anim)
                else -> getDrawable(R.drawable.ic_tab_i_get_anim)
            }
        }.attach()

        // setup view pager on page change listener
        onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (Build.VERSION.SDK_INT >= 24) {
                    (binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)?.icon as? AnimatedVectorDrawable)?.start()
                }
            }
        }
        binding.pager.registerOnPageChangeCallback(onPageChangeCallback)

        // create the menu of the top app bar
        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.settings -> {
                    (it.icon as? AnimatedVectorDrawable)?.start()
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(
                        intent
                    )
                    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
                    true
                }
                else -> false
            }
        }

        // setup the fab
        binding.fab.setOnClickListener {
            when (binding.tabLayout.selectedTabPosition) {
                1 -> CreateDebtFragment.newInstance(0L, iOwe = true)
                    .show(supportFragmentManager, "dialog")
                2 -> CreateDebtFragment.newInstance(0L, iOwe = false)
                    .show(supportFragmentManager, "dialog")
                else -> CreateDebtFragment.newInstance(0L)
                    .show(supportFragmentManager, "dialog")
            }
        }

    }

    fun transformFab(shrink: Boolean) {
        with(binding.fab) {
            if (shrink) shrink() else extend()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // show the rate dialog if not already rated and enough edits were made
            if (!prefs.getBoolean(RateDialog.DISMISS_FOREVER_KEY, false) && prefs.getInt(
                    RateDialog.SINCE_LAST_RATE_KEY,
                    0
                ) >= RateDialog.EDITS_BETWEEN_DIALOGS
            ) {
                RateDialog.newInstance().show(supportFragmentManager, "rate_dialog")

                // reset the edit counter
                prefs.edit()
                    .putInt(RateDialog.SINCE_LAST_RATE_KEY, 0)
                    .apply()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregister Page change listener
        binding.pager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    // inflate the menu in the top app bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_app_bar, menu)
        return true
    }
}
