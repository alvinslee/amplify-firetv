package com.example.amplifyfiretv

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commit()
        }
    }
}

class MainFragment : BrowseSupportFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUIElements()
        loadContent()
    }

    private fun setupUIElements() {
        title = "Amplify Fire TV"
        headersState = HEADERS_DISABLED
    }

    private fun loadContent() {
        val textView = TextView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "Hello Android"
            textSize = 24f
        }

        val container = requireActivity().findViewById<ViewGroup>(R.id.main_browse_fragment)
        container.addView(textView)
    }
}