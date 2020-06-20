package com.eliasball.debtmanager.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.databinding.RateDialogBinding

class RateDialog : DialogFragment() {


    // The sharedPrefs delegated by lazy
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the binding
        val binding = RateDialogBinding.inflate(layoutInflater, container, false)

        // add on change to rating bar
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            when (rating) {
                // bad rating -> feedback
                in 0.0..3.0 -> {
                    binding.send.setText(R.string.send_feedback)
                    binding.actionText.setText(R.string.send_feedback_action)
                    binding.send.setOnClickListener {
                        // send feedback per email
                        val intent = Intent().apply {
                            action = Intent.ACTION_SENDTO
                            data = "mailto:contact.eliasball@gmail.com?subject=Feedback:%20Debt%20Manager&body=Your%20feedback%20here.".toUri()
                        }
                        startActivity(Intent.createChooser(intent, "Send feedback per email"));

                        // dont show rate dialog again
                        prefs.edit()
                            .putBoolean(DISMISS_FOREVER_KEY, true)
                            .apply()

                        dismiss()
                    }
                }
                // good rating -> rate
                else -> {
                    binding.send.setText(R.string.rate)
                    binding.actionText.setText(R.string.rate_action)
                    // open play store page
                    binding.send.setOnClickListener {
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

                        // dont show rate dialog again
                        prefs.edit()
                            .putBoolean(DISMISS_FOREVER_KEY, true)
                            .apply()

                        dismiss()
                    }
                }
            }
            binding.actionText.visibility = View.VISIBLE
            binding.send.isEnabled = true
        }

        // dismiss button
        binding.cancel.setOnClickListener {
            dismiss()
        }

        // dont show again button
        binding.cancelForever.setOnClickListener {
            prefs.edit()
                .putBoolean(DISMISS_FOREVER_KEY, true)
                .apply()
            dismiss()
        }


        return binding.root
    }

    /*
    Call newInstance().show(supportFragmentManager, "rate_dialog") to show the rate dialog
     */
    companion object {
        fun newInstance() =
            RateDialog()

        const val DISMISS_FOREVER_KEY = "dismiss_forever"
        const val SINCE_LAST_RATE_KEY = "since_last_rate"

        // How many debts have to be modified or created between showing the rating dialog
        const val EDITS_BETWEEN_DIALOGS = 5

    }
}