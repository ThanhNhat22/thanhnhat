package com.app.findback.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.R
import com.app.findback.databinding.FragmentHomeBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.app.findback.ui.activities.PostDetailActivity
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

    private var isShowSearch = false
    private var iconIB2 = R.drawable.ic_search
    private var currentQuery: String = ""
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

    //bắt sự kiện sreach
    private fun setEventClickSearch()
    {
        isShowSearch = !isShowSearch
        iconIB2 = if (isShowSearch) R.drawable.ic_close else R.drawable.ic_search
        (requireActivity() as? BaseBottomNavActivity)?.refreshToolbarForActiveFragment()

        val searchInput = (activity as? BaseBottomNavActivity)?.getToolbarSearchInput() ?: return
        if (isShowSearch) {
            searchInput.requestFocus()
            searchInput.setSelection(searchInput.text?.length ?: 0)
            showKeyboard(searchInput)
        } else {
            searchInput.clearFocus()
            searchInput.setText("")
            hideKeyboard(searchInput)
            currentQuery = ""
        }
    }
    private fun showKeyboard(view: View) {
        view.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    //fillter theo chip
    private fun filterPosts(){
        //set mặc định là tất cả
        binding.cgChip.check(binding.cgChip.getChildAt(0).id)
        binding.cgChip.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.id == checkedId) {
                   when(chip.text){
                       "Tất cả" -> {
                           homeAdapter.addNewData(allPost)
                       }
                       "Thất lạc" -> {
                           homeAdapter.addNewData(allPost.filter { it.postType == "lost" })
                       }
                       "Tìm thấy" -> {
                           homeAdapter.addNewData(allPost.filter { it.postType == "found" })
                       }
                       "Gần tôi" -> {

                       }
                       else -> {
                           homeAdapter.addNewData(allPost)
                       }
                   }
                }
            }
        }
    }
    //set recyclerview
    private fun setRecyclerView(){
        //setup normal posts
        val layoutManager = GridLayoutManager(requireContext(), 1)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvPosts.layoutManager = layoutManager
        binding.rvPosts.adapter = homeAdapter

        //setup vip posts
        val layoutManagerVip = GridLayoutManager(requireContext(), 1)
        layoutManagerVip.orientation = LinearLayoutManager.HORIZONTAL
        binding.rvPostsSuggest.layoutManager = layoutManagerVip
        binding.rvPostsSuggest.adapter = homeAdapter

        homeAdapter.setOnItemClickListener(object : HomeAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
               val postId = allPost[position].postId
                Log.d("HomeFragment", "Clicked postId: $postId")
                val intent = android.content.Intent(requireContext(), PostDetailActivity::class.java)
                intent.putExtra("postId", postId)
                startActivity(intent)
            }

            override fun onItemClickShare(position: Int) {
                    val postId = allPost[position].postId
                    Log.d("BaseBottomNavActivity",postId)
                    val link = "https://metalk-a52fb.web.app/post/$postId"

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, link)
                    }
                    startActivity(Intent.createChooser(intent, "Chia sẻ"))
            }

            override fun onItemClickSave(position: Int) {
                Toast.makeText(requireContext(), "CHưa làm gì cả", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        postViewModel.removeListener()
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            titleResId = R.string.app_name,
            isShowSearch = isShowSearch,
            isBack = false,
            imageLogoRes = R.drawable.logo_tran,
            ib1Res = R.drawable.ic_notification,
            ib2Res =iconIB2,
            onIB2 = { setEventClickSearch() }
        )
    }
}