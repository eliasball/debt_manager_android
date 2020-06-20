package com.eliasball.debtmanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager


class SplashScreen : AppCompatActivity() {

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!sharedPreferences.getBoolean(SettingsActivity.TERMS_AND_PRIVACY_KEY, false)){
            // Launch the entry activity to show privacy policy
            val intent = Intent(this, EntryActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Launch the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
