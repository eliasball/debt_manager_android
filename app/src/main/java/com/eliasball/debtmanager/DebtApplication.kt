package com.eliasball.debtmanager

import android.app.Application
import androidx.preference.PreferenceManager
import com.eliasball.debtmanager.data.db.DebtDao
import com.eliasball.debtmanager.data.db.DebtDatabase
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.data.providers.ShareParser
import com.eliasball.debtmanager.data.repository.DebtRepository
import com.eliasball.debtmanager.data.repository.DebtRepositoryImpl
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class DebtApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@DebtApplication))

        bind<DebtDatabase>() with singleton { DebtDatabase(instance()) }
        bind<DebtDao>() with singleton { instance<DebtDatabase>().debtDao() }
        bind<DebtRepository>() with singleton { DebtRepositoryImpl(instance()) }
        bind<CurrencyProvider>() with provider { CurrencyProvider(instance()) }
        bind<ShareParser>() with provider { ShareParser() }
    }

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
    }
}