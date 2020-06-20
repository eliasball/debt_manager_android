package com.eliasball.debtmanager.data.providers

import android.content.Context
import com.eliasball.debtmanager.internal.isolateCurrencySymbol
import com.eliasball.debtmanager.ui.SettingsActivity
import java.util.*

class CurrencyProvider(context: Context) : PreferenceProvider(context) {

    fun getCurrencySymbol(): String {
        return Currency.getInstance(
            preferences.getString(
                SettingsActivity.CURRENCY_KEY,
                "EUR"
            )
        ).symbol.isolateCurrencySymbol()
    }
}