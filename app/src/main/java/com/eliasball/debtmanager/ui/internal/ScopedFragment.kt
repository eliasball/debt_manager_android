package com.eliasball.debtmanager.ui.internal

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class ScopedFragment: Fragment(), CoroutineScope {
    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}