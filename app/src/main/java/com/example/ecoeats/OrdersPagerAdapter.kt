package com.example.ecoeats

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OrdersPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var ongoingOrders: List<Order> = emptyList()
    private var historyOrders: List<Order> = emptyList()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OrdersFragment.newInstance(historyOrders)
            1 -> OrdersFragment.newInstance(ongoingOrders)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    fun setOrders(ongoing: List<Order>, history: List<Order>) {
        ongoingOrders = ongoing
        historyOrders = history
        notifyDataSetChanged()
    }
}
