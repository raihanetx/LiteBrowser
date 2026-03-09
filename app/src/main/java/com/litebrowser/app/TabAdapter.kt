package com.litebrowser.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(fa: FragmentActivity, private val tabUrls: MutableList<String>) :
    FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = tabUrls.size

    override fun createFragment(position: Int): Fragment {
        return WebViewFragment().apply {
            arguments = Bundle().apply {
                putString("url", tabUrls[position])
                putInt("position", position) // pass position for later use
            }
        }
    }

    fun addTab(url: String = "https://www.google.com") {
        tabUrls.add(url)
        notifyItemInserted(tabUrls.size - 1)
    }

    fun removeTab(position: Int) {
        if (position in tabUrls.indices) {
            tabUrls.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
