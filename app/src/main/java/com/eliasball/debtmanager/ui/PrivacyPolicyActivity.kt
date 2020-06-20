package com.eliasball.debtmanager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eliasball.debtmanager.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the action bar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Load the licenses html file
        binding.webView.loadUrl("file:///android_asset/privacy_policy.htm")
    }
}
