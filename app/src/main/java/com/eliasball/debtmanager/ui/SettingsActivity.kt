package com.eliasball.debtmanager.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.databinding.SettingsActivityBinding
import com.eliasball.debtmanager.internal.goToUrl
import com.eliasball.debtmanager.internal.isolateCurrencySymbol
import kotlinx.coroutines.*
import java.util.*


const val TWITTER_URL = "https://twitter.com/That_BlueStone"
const val GITHUB_URL = "https://github.com/blue1stone"
const val BEHANCE_URL = "https://www.behance.net/eliasball"
const val UPWORK_URL = "https://www.upwork.com/freelancers/~01a12e1ef396b9dc35"

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the binding and set it
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup the action bar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup the social media buttons
        binding.twitter.setOnClickListener {
            goToUrl(TWITTER_URL)
        }
        binding.github.setOnClickListener {
            goToUrl(GITHUB_URL)
        }
        binding.behance.setOnClickListener {
            goToUrl(BEHANCE_URL)
        }
        binding.upwork.setOnClickListener {
            goToUrl(UPWORK_URL)
        }

        // Add the settings to the layout
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Animate the window on back press
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val job = Job()
        private val coroutineScope = CoroutineScope(job + Dispatchers.Main)

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Coroutine for getting and formatting all possible currencies
            coroutineScope.launch {
                // disable the preference while getting the data
                findPreference<ListPreference>(CURRENCY_KEY)?.isEnabled = false

                // Get all currencies in a list
                val currencies = withContext(Dispatchers.Default) {
                    Currency.getAvailableCurrencies().sortedBy {
                        it.displayName
                    }.filter {
                        !it.displayName.matches(".*\\d.*".toRegex())
                    }
                }
                // Setup the entries of the currency list preference
                findPreference<ListPreference>(CURRENCY_KEY)?.entries =
                    currencies.map {
                        "${it.displayName} (${it.symbol.isolateCurrencySymbol()})"
                    }.toTypedArray()
                findPreference<ListPreference>(CURRENCY_KEY)?.entryValues =
                    currencies.map {
                        it.currencyCode
                    }.toTypedArray()

                // enable the preference again
                findPreference<ListPreference>(CURRENCY_KEY)?.isEnabled = true
            }

            // Setup the rate app preference
            findPreference<Preference>("rate_pref")?.setOnPreferenceClickListener {
                val appPackageName = requireContext().packageName
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
                true
            }

            // Setup the licenses preference
            findPreference<Preference>("licenses_pref")?.setOnPreferenceClickListener {
                startActivity(Intent(requireActivity(), LicenseActivity::class.java))
                true
            }

            // Setup the privacy preference
            findPreference<Preference>("privacy_pref")?.setOnPreferenceClickListener {
                startActivity(Intent(requireActivity(), PrivacyPolicyActivity::class.java))
                true
            }

            // Setup the terms preference
            findPreference<Preference>("terms_pref")?.setOnPreferenceClickListener {
                startActivity(Intent(requireActivity(), TermsActivity::class.java))
                true
            }

            // Setup the feedback preference
            findPreference<Preference>("send_feedback")?.setOnPreferenceClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = "mailto:contact.eliasball@gmail.com?subject=Feedback:%20Debt%20Manager&body=Your%20feedback%20here.".toUri()
                }
                startActivity(Intent.createChooser(intent, "Send feedback per email"))
                true
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            // cancel all the coroutines in the job
            job.cancel()
        }
    }

    companion object {
        // The key for the currency sharedPref and the setting
        const val CURRENCY_KEY = "currency"

        // The key for the privacy accepted preference
        const val TERMS_AND_PRIVACY_KEY = "terms_and_privacy_accepted"
    }
}