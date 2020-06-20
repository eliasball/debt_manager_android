package com.eliasball.debtmanager.ui.adapters

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.eliasball.debtmanager.ui.fragments.IGetFragment
import com.eliasball.debtmanager.ui.fragments.IOweFragment
import com.eliasball.debtmanager.ui.fragments.PeopleFragment

class ViewPagerAdapter(activity: Activity): FragmentStateAdapter(activity as FragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> PeopleFragment()
            1 -> IOweFragment()
            else -> IGetFragment()
        }
    }


}