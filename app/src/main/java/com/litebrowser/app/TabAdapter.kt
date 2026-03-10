package com.litebrowser.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(
    private val activity: FragmentActivity,
    private val urls: MutableList<String>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = urls.size

    override fun createFragment(position: Int): Fragment {
        return WebViewFragment.newInstance(urls[position], position)
    }

    fun addTab(url: String = "https://www.google.com") {
        urls.add(url)
        notifyItemInserted(urls.size - 1)
    }

    fun removeTab(position: Int) {
        if (position in urls.indices && urls.size > 1) {
            urls.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, urls.size)
        }
    }

    fun getTabCount(): Int = urls.size

    fun getUrls(): List<String> = urls.toList()
}
