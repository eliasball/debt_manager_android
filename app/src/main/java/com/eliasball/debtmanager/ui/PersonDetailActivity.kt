package com.eliasball.debtmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.db.entities.PersonDetail
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.data.providers.ShareParser
import com.eliasball.debtmanager.data.repository.DebtRepository
import com.eliasball.debtmanager.databinding.ActivityPersonDetailBinding
import com.eliasball.debtmanager.internal.ScrollAware
import com.eliasball.debtmanager.internal.slideEnter
import com.eliasball.debtmanager.internal.slideExit
import com.eliasball.debtmanager.ui.adapters.DebtRecyclerAdapter
import com.eliasball.debtmanager.ui.fragments.CreateDebtFragment
import kotlinx.coroutines.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class PersonDetailActivity : AppCompatActivity(), KodeinAware, ScrollAware {

    // The injected variables
    override val kodein by closestKodein()
    private val debtRepository by instance<DebtRepository>()
    private val currencyProvider by instance<CurrencyProvider>()
    private val shareParser by instance<ShareParser>()

    // The binding variables
    private lateinit var binding: ActivityPersonDetailBinding

    // Coroutine setup
    private var job = Job()
    private val coroutineScope = CoroutineScope(job + Dispatchers.Main)

    // The data required for recycler and other views
    private lateinit var debts: LiveData<List<Debt>>
    private lateinit var personDetail: LiveData<PersonDetail>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the binding
        binding = ActivityPersonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // setup the action bar
        setSupportActionBar(binding.bar)
        binding.bar.setNavigationOnClickListener {
            onBackPressed()
        }

        // create the layout manager
        binding.recycler.layoutManager = LinearLayoutManager(this)
        // Create and attach the debt adapter
        val adapter = DebtRecyclerAdapter(mutableListOf(), currencyProvider, true,
            onSendClick = {
                coroutineScope.launch {
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
                    val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_debt))
                    startActivity(shareIntent)
                }
            },
            onTickClick = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        debtRepository.deleteDebt(it)
                    }
                }
            },
            onContainerClick = {
                CreateDebtFragment.newInstance(it.date)
                    .show(supportFragmentManager, "dialog")
            })
        binding.recycler.adapter = adapter

        // get and observe the data for the recycler
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                debts = debtRepository.getDebtsByPerson(intent.getStringExtra(EXTRA_NAME)!!)
            }
            debts.observe(this@PersonDetailActivity, Observer {
                if(it.isNotEmpty()){
                    binding.backdrop.backdrop.visibility = View.GONE
                } else {
                    binding.backdrop.backdrop.visibility = View.VISIBLE
                }
                adapter.setData(it)
            })
        }


        binding.personName.text = intent.getStringExtra(EXTRA_NAME)

        // Get and observe the data for the views
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                personDetail = debtRepository.getPersonDetails(intent.getStringExtra(EXTRA_NAME)!!)
            }
            personDetail.observe(this@PersonDetailActivity, Observer {
                binding.totalText.text = if (it.moneyCount > 0) getString(
                    R.string.total_text,
                    it.total,
                    currencyProvider.getCurrencySymbol()
                ) else getString(R.string.total_empty_text)
                binding.moneyCountText.text = getString(R.string.count_text, it.moneyCount)
                binding.objectsCountText.text = getString(R.string.count_text, it.objectsCount)
            })
        }

        // set the fab to create a new debt for this person
        binding.fab.setOnClickListener {
            CreateDebtFragment.newInstance(0L, person = intent.getStringExtra(EXTRA_NAME)!!)
                .show(supportFragmentManager, "dialog")
        }

        // Implement share button
        binding.sendAsText.setOnClickListener {
            coroutineScope.launch {
                // Get the text
                val text = shareParser.getShareDebtsForPersonString(
                    intent.getStringExtra(EXTRA_NAME)!!,
                    debtRepository,
                    currencyProvider.getCurrencySymbol()
                )
                // Create the intent
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_debt))
                startActivity(shareIntent)
            }
        }

        // make the toolbar appear and disappear on scroll
        attachToScroll(binding.recycler, {
            binding.toolbar.slideExit()
        }, {
            binding.toolbar.slideEnter()
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Animate the window
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right)
    }

    companion object {
        // The extra for the person name
        const val EXTRA_NAME = "EXTRA_NAME"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines on the job
        job.cancel()
    }
}
