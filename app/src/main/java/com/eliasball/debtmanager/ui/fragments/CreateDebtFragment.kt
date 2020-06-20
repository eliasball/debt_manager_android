package com.eliasball.debtmanager.ui.fragments

import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.data.repository.DebtRepository
import com.eliasball.debtmanager.databinding.DialogFragmentCreateDebtBinding
import com.eliasball.debtmanager.internal.DebtTypes
import com.eliasball.debtmanager.internal.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import kotlin.math.abs


// the intent args
const val ARG_DEBT_DATE = "DEBT_DATE"
const val ARG_PERSON = "ARG_PERSON"
const val ARG_I_OWE = "ARG_I_OWE"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    CreateDebtFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class CreateDebtFragment : BottomSheetDialogFragment(), KodeinAware {

    // the injected variables
    override val kodein by closestKodein()
    private val debtRepository by instance<DebtRepository>()
    private val currencyProvider by instance<CurrencyProvider>()

    // the binding variables
    private var _binding: DialogFragmentCreateDebtBinding? = null
    private val binding get() = _binding!!

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    // the coroutine setup
    private val job = Job()
    private val coroutineScope = CoroutineScope(job + Dispatchers.Main)

    // The debt to be editied
    private lateinit var debt: Debt
    // Is an edit being done or is a new debt being created?
    private var debtExisted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFragmentCreateDebtBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        coroutineScope.launch {
            // Get the relevant debt
            if (savedInstanceState != null) {
                // the fragment was rebuilt
                debt = savedInstanceState.getParcelable(DEBT_KEY)!!
                debtExisted = savedInstanceState.getBoolean(EXISTED_KEY)
            } else {
                // Changing an existing debt
                if (arguments?.getLong(ARG_DEBT_DATE) != 0L) {
                    debt = withContext(Dispatchers.IO) {
                        debtRepository.getDebt(requireArguments().getLong(ARG_DEBT_DATE))
                    }
                    debtExisted = true
                } else {
                    // Creating a new debt
                    debt = Debt()
                    debt.iOwe = arguments?.getBoolean(ARG_I_OWE) ?: false
                    debt.person = arguments?.getString(ARG_PERSON) ?: ""
                }
            }

            if (debtExisted){
                binding.title.setText(R.string.change_debt_header)
            } else {
                binding.title.setText(R.string.create_new_debt_header)
            }

            binding.personTextField.editText?.setText(debt.person)
            binding.reasonTextField.editText?.setText(debt.reason)


            // Add the listener to the direction toggle group
            binding.toggleDirectionButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {
                    // Animate the button
                    if(Build.VERSION.SDK_INT >= 24) {
                        (group.findViewById<MaterialButton>(checkedId).icon as? AnimatedVectorDrawable)?.start()
                    }
                    // Change the debt iOwe
                    debt.iOwe = when (checkedId) {
                        R.id.iGetBtn -> false
                        else -> true
                    }
                } else {
                    if (group.checkedButtonIds.isEmpty()) {
                        group.check(checkedId)
                    }
                }
            }

            // Add the listener to the type toggle group
            binding.toggleTypeButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {
                    // Animate the button
                    if(Build.VERSION.SDK_INT >= 24) {
                        (group.findViewById<MaterialButton>(checkedId).icon as? AnimatedVectorDrawable)?.start()
                    }
                    // Change the debt type
                    when (checkedId) {
                        R.id.moneyBtn -> {
                            debt.type = DebtTypes.MONEY.type
                            debt.objects = ""
                            with(binding.amountObjectsTextField) {
                                editText?.text!!.clear()
                                editText?.inputType =
                                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                hint = getString(
                                    R.string.amount_text,
                                    currencyProvider.getCurrencySymbol()
                                )
                            }
                        }
                        R.id.objectsBtn -> {
                            debt.type = DebtTypes.OBJECTS.type
                            debt.amount = 0.0
                            with(binding.amountObjectsTextField) {
                                editText?.text!!.clear()
                                editText?.inputType =
                                    InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                                hint = getString(R.string.objects_text)
                            }
                        }
                    }
                } else {
                    if (group.checkedButtonIds.isEmpty()) {
                        group.check(checkedId)
                    }
                }
            }

            // Setup the Amount field
            when (debt.type) {
                DebtTypes.MONEY.type -> {
                    binding.moneyBtn.performClick()
                    binding.amountObjectsTextField.editText?.setText(
                        if (debt.amount != 0.0) "%.2f".format(
                            abs(debt.amount)
                        ).replace(",",".") else ""
                    )
                }
                else -> {
                    binding.objectsBtn.performClick()
                    binding.amountObjectsTextField.editText?.setText(debt.objects)
                }
            }

            // Process the iOwe state
            if (debt.iOwe) {
                binding.iOweBtn.performClick()
            } else binding.iGetBtn.performClick()

            // Setup the autocomplete
            val names = withContext(Dispatchers.IO) {
                debtRepository.getNames()
            }
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            (binding.personTextField.editText as AutoCompleteTextView).setAdapter(adapter)
        }

        // Make the "enter" button on the last edit text save the debt
        binding.amountObjectsTextField.editText?.setOnEditorActionListener { _, actionId, _ ->
            if ((actionId == EditorInfo.IME_ACTION_SEND)) {
                binding.confirmBtn.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // Add OnClickEvents to cancel and confirm button
        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        binding.confirmBtn.setOnClickListener {
            hideKeyboard()

            // check if all entries are valid and retrieve the data into the debt
            var entriesValid = true
            with(binding) {
                if (personTextField.editText?.text!!.isNotBlank()) {
                    with(personTextField) {
                        debt.person = editText!!.text.toString()
                        error = null
                    }
                } else {
                    entriesValid = false
                    personTextField.error = getString(R.string.error_text)
                }
                if (reasonTextField.editText?.text!!.isNotBlank()) {
                    with(reasonTextField) {
                        debt.reason = editText!!.text.toString()
                        error = null
                    }
                } else {
                    entriesValid = false
                    reasonTextField.error = getString(R.string.error_text)
                }
                if (amountObjectsTextField.editText?.text!!.isNotBlank()) {
                    when (debt.type) {
                        DebtTypes.MONEY.type -> {
                            try {
                                debt.amount =
                                    if (debt.iOwe) -amountObjectsTextField.editText!!.text.toString()
                                        .toDouble() else amountObjectsTextField.editText!!.text.toString()
                                        .toDouble()
                            } catch (e: Exception){
                                Toast.makeText(context, getString(R.string.wrong_number_entered), Toast.LENGTH_LONG).show()
                                entriesValid = false
                                amountObjectsTextField.error = getString(R.string.wrong_input)
                            }

                            debt.objects = ""
                        }
                        DebtTypes.OBJECTS.type -> {
                            debt.objects = amountObjectsTextField.editText!!.text.toString()
                            debt.amount = 0.0
                        }
                    }
                    amountObjectsTextField.error = null
                } else {
                    entriesValid = false
                    amountObjectsTextField.error = getString(R.string.error_text)
                }
            }

            // if valid save the debt
            if (entriesValid) {
                coroutineScope.launch {
                    // save the debt
                    withContext(Dispatchers.IO) {
                        debtRepository.upsertDebt(debt)
                    }

                    // add one edit to the show rate dialog counter
                    if (!prefs.getBoolean(RateDialog.DISMISS_FOREVER_KEY, false)) {
                        prefs.edit()
                            .putInt(
                                RateDialog.SINCE_LAST_RATE_KEY,
                                prefs.getInt(RateDialog.SINCE_LAST_RATE_KEY, 0) + 1
                            )
                            .apply()
                    }

                    // dismiss the create dialog
                    dismiss()
                }
            }
        }
    }

    companion object {

        // Create a new CreateDebtFragment and supply all the necessary debt info
        fun newInstance(
            debtDate: Long,
            iOwe: Boolean = false,
            person: String = ""
        ): CreateDebtFragment =
            CreateDebtFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_DEBT_DATE, debtDate)
                    putBoolean(ARG_I_OWE, iOwe)
                    putString(ARG_PERSON, person)
                }
            }

        // the onSaveInstanceState bundle key
        const val DEBT_KEY = "debt_key"
        const val EXISTED_KEY = "debt_existed"

    }

    override fun onSaveInstanceState(outState: Bundle) {
        // save the debt
        outState.putParcelable(DEBT_KEY, debt)
        outState.putBoolean(EXISTED_KEY, debtExisted)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the binding
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // cancel all the coroutines in the job
        job.cancel()
    }
}
