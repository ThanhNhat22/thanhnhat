package com.app.findback.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.R
import com.app.findback.databinding.FragmentHomeBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.adapters.HomeAdapter
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.PostViewModel
import com.google.android.material.chip.Chip

class HomeFragment : Fragment(), ToolbarConfigProvider {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var allPost: List<Post>
    private lateinit var postViewModel: PostViewModel

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
        setControl()
        setEvent()
    }
    //set control
    private fun setControl(){
        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]
        homeAdapter = HomeAdapter(requireContext(), mutableListOf())
    }
    //set event
    private fun setEvent(){
        getDataFromViewModel()
        setRecyclerView()
        filterPosts()
    }
    //lấy dữ liệu từ viewModel
    private fun getDataFromViewModel(){
        postViewModel.postsShared.observe(viewLifecycleOwner) { posts ->
            allPost = posts
            homeAdapter.addNewData(allPost)
        }
    }
    //fillter theo chip
    private fun filterPosts(){
        //set mặc định là tất cả
        binding.cgChip.check(binding.cgChip.getChildAt(0).id)
        binding.cgChip.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.id == checkedId) {

                }
            }
        }
    }
    //set recyclerview
    private fun setRecyclerView(){
        val layoutManager = GridLayoutManager(requireContext(), 1)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvPosts.layoutManager = layoutManager
        binding.rvPosts.adapter = homeAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        postViewModel.removeListener()
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            titleResId = R.string.app_name,
            isBack = false,
            isShowSearch = false,
            imageLogoRes = R.drawable.logo_tran,
            ib1Res = R.drawable.ic_notification,
            ib2Res = R.drawable.ic_search
        )
    }
}