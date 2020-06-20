package com.eliasball.debtmanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.eliasball.debtmanager.databinding.ActivityEntryBinding

class EntryActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.terms.setOnClickListener {
            val intent = Intent(this, TermsActivity::class.java)
            startActivity(intent)
        }

        binding.privacyCheck.setOnCheckedChangeListener { _, isChecked ->
            binding.continueBtn.isEnabled = isChecked
        }

        binding.quitBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.continueBtn.setOnClickListener {
            sharedPreferences.edit()
                .putBoolean(SettingsActivity.TERMS_AND_PRIVACY_KEY, true)
                .apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
