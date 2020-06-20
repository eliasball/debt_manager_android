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
import androidx.recyclerview.widget.GridLayoutManager
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.data.db.entities.PersonDebt
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.data.repository.DebtRepository
import com.eliasball.debtmanager.databinding.FragmentPeopleBinding
import com.eliasball.debtmanager.internal.ScrollAware
import com.eliasball.debtmanager.ui.MainActivity
import com.eliasball.debtmanager.ui.PersonDetailActivity
import com.eliasball.debtmanager.ui.SettingsActivity
import com.eliasball.debtmanager.ui.adapters.PeopleRecyclerAdapter
import com.eliasball.debtmanager.ui.internal.ScopedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class PeopleFragment() : ScopedFragment(), KodeinAware, ScrollAware {

    // Get the injected variables
    override val kodein by closestKodein()
    private val debtRepository by instance<DebtRepository>()
    private val currencyProvider by instance<CurrencyProvider>()

    // The binding vars
    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!

    // The PreferenceChangedListener
    private lateinit var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    // the data for the recycler
    private lateinit var debtsPerPeople: LiveData<List<PersonDebt>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using viewBinding
        _binding = FragmentPeopleBinding.inflate(layoutInflater, container, false)

        // set the layoutManager of the recycler
        binding.peopleRecycler.layoutManager = GridLayoutManager(context, 2)

        // create the recyclerAdapter for the recycler and attach it
        val peopleRecyclerAdapter =
            PeopleRecyclerAdapter(mutableListOf(), currencyProvider) { name, view ->
                // start the detail activity for the person
                val intent = Intent(requireActivity(), PersonDetailActivity::class.java)
                intent.putExtra(PersonDetailActivity.EXTRA_NAME, name)
                startActivity(
                    intent
//                    ActivityOptions.makeSceneTransitionAnimation(
//                        requireActivity(),
//                        Pair(view, "card_bloat")
//                    ).toBundle()
                )
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
            }
        binding.peopleRecycler.adapter = peopleRecyclerAdapter

        // get the debtsPerPeople from the repository
        launch {
            withContext(Dispatchers.IO) {
                debtsPerPeople = debtRepository.getDebtsPerPeople()
            }
            // automatically update the recycler when new data is observed
            debtsPerPeople.observe(viewLifecycleOwner, Observer {
                if(it.isNotEmpty()){
                    binding.backdrop.backdrop.visibility = View.GONE
                } else {
                    binding.backdrop.backdrop.visibility = View.VISIBLE
                }
                peopleRecyclerAdapter.setData(it)
            })
            // after loading the data make the recycler visible
            binding.progress.visibility = View.GONE
            binding.peopleRecycler.visibility = View.VISIBLE
        }

        // attach the fab of the MainActivity to the recyclerScroll to shrink and extend it
        attachToScroll(binding.peopleRecycler, {
            (requireActivity() as MainActivity).transformFab(true)
        }, {
            (requireActivity() as MainActivity).transformFab(false)
        })

        // create and register the preferenceChangeListener
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener() { _, key ->
            // if the currency changed, then update the recycler
            if (key == SettingsActivity.CURRENCY_KEY) {
                binding.peopleRecycler.adapter?.notifyDataSetChanged()
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clean up the listener
        PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        // clean up the binding
        _binding = null

    }
}
