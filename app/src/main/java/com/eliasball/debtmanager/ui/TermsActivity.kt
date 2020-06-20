package com.eliasball.debtmanager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eliasball.debtmanager.databinding.ActivityTermsBinding

class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the action bar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Load the licenses html file
        binding.webView.loadUrl("file:///android_asset/terms_and_conditions.htm")
    }
}
