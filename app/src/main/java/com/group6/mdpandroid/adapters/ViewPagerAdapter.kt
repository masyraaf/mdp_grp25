package com.group6.mdpandroid.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.group6.mdpandroid.fragments.Bluetooth
import com.group6.mdpandroid.fragments.RobotController
import com.group6.mdpandroid.fragments.StatusDisplay

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                RobotController()
            }
            1 -> {
                StatusDisplay()
            }
            2 -> {
                Bluetooth()
            }
            else -> {
                Fragment()
            }
        }
    }
}