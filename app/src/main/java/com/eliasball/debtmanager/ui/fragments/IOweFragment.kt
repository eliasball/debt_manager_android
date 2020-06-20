package com.eliasball.debtmanager.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.data.providers.ShareParser
import com.eliasball.debtmanager.data.repository.DebtRepository
import com.eliasball.debtmanager.databinding.FragmentIOweBinding
import com.eliasball.debtmanager.internal.ScrollAware
import com.eliasball.debtmanager.ui.MainActivity
import com.eliasball.debtmanager.ui.SettingsActivity
import com.eliasball.debtmanager.ui.adapters.DebtRecyclerAdapter
import com.eliasball.debtmanager.ui.internal.ScopedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class IOweFragment : ScopedFragment(), KodeinAware, ScrollAware{

    // Get the injected variables
    override val kodein by closestKodein()
    private val debtRepository by instance<DebtRepository>()
    private val currencyProvider by instance<CurrencyProvider>()
    private val shareParser by instance<ShareParser>()

    // The binding vars
    private var _binding: FragmentIOweBinding? = null
    private val binding get() = _binding!!

    // the shared preference listener
    private lateinit var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    // The data pushed to the recycler
    private lateinit var debts: LiveData<List<Debt>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the binding for this fragment
        _binding = FragmentIOweBinding.inflate(layoutInflater, container, false)

        // Attach the layout manager to the recylcer
        binding.iOweRecycler.layoutManager = LinearLayoutManager(context)

        // Create and add the adapter for the debt recycler
        val debtRecyclerAdapter = DebtRecyclerAdapter(mutableListOf(),
            currencyProvider,
            onSendClick = {
                launch {
                    // Get the text
                    val text = shareParser.getShareDebtString(
                        it,
                        debtRepository,
                        currencyProvider.getCurrencySymbol()
                    )
                    // Create the intent
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(sendIntent, getString(R.string.share_debt))
                    startActivity(shareIntent)
                }
            },
            onTickClick = {
                launch {
                    withContext(Dispatchers.IO) {
                        debtRepository.deleteDebt(it)
                    }
                }
            },
            onContainerClick = {
                CreateDebtFragment.newInstance(it.date)
                    .show(requireActivity().supportFragmentManager, "dialog")
            })
        binding.iOweRecycler.adapter = debtRecyclerAdapter

        // get and observe the data for the recycler from the repository
        launch {
            withContext(Dispatchers.IO) {
                debts = debtRepository.getIOweOthers()
            }
            debts.observe(viewLifecycleOwner, Observer {
                if(it.isNotEmpty()){
                    binding.backdrop.backdrop.visibility = View.GONE
                } else {
                    binding.backdrop.backdrop.visibility = View.VISIBLE
                }
                debtRecyclerAdapter.setData(it)
            })
            binding.progress.visibility = View.GONE
            binding.iOweRecycler.visibility = View.VISIBLE
        }

        // Shrink and extend the fab on scroll
        attachToScroll(binding.iOweRecycler, {
            (requireActivity() as MainActivity).transformFab(true)
        }, {
            (requireActivity() as MainActivity).transformFab(false)
        })

        // Add the sharedPref listener
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SettingsActivity.CURRENCY_KEY) {
                binding.iOweRecycler.adapter?.notifyDataSetChanged()
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // remove the sharedPrefListener
        PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        // Remove the binding
        _binding = null
    }

}
