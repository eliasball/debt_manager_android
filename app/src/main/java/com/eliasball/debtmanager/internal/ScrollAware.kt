package com.eliasball.debtmanager.internal

import androidx.recyclerview.widget.RecyclerView

interface ScrollAware {

    fun attachToScroll(
        recyclerView: RecyclerView,
        onUpScroll: () -> Unit,
        onDownScroll: () -> Unit
    ) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) onUpScroll() else onDownScroll()
            }
        })
    }

}