package com.app.findback.ui.components.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.findback.databinding.LayoutMapBottomSheetBinding
import com.app.findback.domain.repositories.model.Post

class MapBottomSheet(private val post: Post) : BaseBottomSheet() {
     private var _binding: LayoutMapBottomSheetBinding? = null
     private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutMapBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
}