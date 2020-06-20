package com.eliasball.debtmanager.data.providers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

abstract class PreferenceProvider(context: Context) {
    private val applicationContext = context.applicationContext

    protected val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(applicationContext)
}