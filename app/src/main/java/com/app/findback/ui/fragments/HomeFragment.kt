package com.app.findback.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.findback.R
import com.app.findback.databinding.FragmentHomeBinding
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider

class HomeFragment : Fragment(), ToolbarConfigProvider {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            titleResId = R.string.app_name,
            isBack = false,
            imageLogoRes = R.drawable.logo_tran,
            ib1Res = R.drawable.ic_notification,
            ib2Res = R.drawable.ic_search
        )
    }
}