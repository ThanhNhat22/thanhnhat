package com.app.findback.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.databinding.ActivitySearchPostBinding
import com.app.findback.domain.models.Post
import com.app.findback.domain.models.SearchHistory
import com.app.findback.ui.adapters.HomeAdapter
import com.app.findback.ui.adapters.SearchHistoryAdapter
import com.app.findback.ui.viewmodel.PostViewModel
import com.app.findback.ui.viewmodel.SearchHistoryViewModel
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SearchPostActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchPostBinding
    private lateinit var postViewModel: PostViewModel
    private lateinit var searchHistoryViewModel: SearchHistoryViewModel
    private lateinit var postAdapter : HomeAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private var allPost: List<Post>? = null
    private var allSearchHistory: List<SearchHistory>? = null
    private var searchTextWatcher: TextWatcher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setControl()
        setEvent()
    }
    private fun setControl(){
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        searchHistoryViewModel = ViewModelProvider(this)[SearchHistoryViewModel::class.java]
        postAdapter = HomeAdapter(this, mutableListOf())
        searchHistoryAdapter = SearchHistoryAdapter(this, mutableListOf())
        setRcv()
    }
    private fun setEvent(){
        searchHistoryViewModel.getSearchHistory("1234")
        setUpToolbar()
        getDataFromViewModel()
        queryData()
        setOnClick()
        filterPosts()
    }
    //setrcrvire post
    private fun setRcv() {
        //cấu hình luôn recycview
        val layoutManager = GridLayoutManager(this, 1)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvPost.layoutManager = layoutManager
        binding.rvPost.adapter = postAdapter

        val layoutManager1 = GridLayoutManager(this, 1)
        layoutManager1.orientation = LinearLayoutManager.VERTICAL
        binding.rvRecently.layoutManager = layoutManager1
        binding.rvRecently.adapter = searchHistoryAdapter
        binding.rvRecently.visibility = View.VISIBLE
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
                        "Bài viết" -> {
                            binding.rvPost.visibility = View.VISIBLE
                        }
                        "Mọi người" -> {
                            binding.rvPost.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
    //set xự kiện
    private fun setOnClick(list: List<Post> = emptyList()) {
          postAdapter.setOnItemClickListener(object : HomeAdapter.OnItemClickListener {
              override fun onItemClick(position: Int) {

                  if (binding.toolbarLayout.etSearch.text.toString().isNotEmpty()) {


                      //tạo mới 1 đối tượng searchhistory
                      val searchHistory = SearchHistory(
                          id = UUID.randomUUID().toString(),
                          userId = "1234",
                          content = binding.toolbarLayout.etSearch.text.toString(),
                          createdAt = System.currentTimeMillis().toString(),
                          updatedAt = System.currentTimeMillis().toString()
                      )
                      //lưu vào history
                      searchHistoryViewModel.createSearchHistory(searchHistory,onSuccess = {
                          searchHistoryAdapter.addData(searchHistory)

                          allSearchHistory = allSearchHistory?.toMutableList()?.apply {
                              add(searchHistory)
                          }
                      })
                  }
                  val postId = list[position].postId
                  val intent = Intent(this@SearchPostActivity, PostDetailActivity::class.java)
                  intent.putExtra("postId", postId)
                  startActivity(intent)
              }

              override fun onItemClickShare(position: Int) {
                  val postId = list[position].postId
                  Log.d("BaseBottomNavActivity",postId)
                  val link = "https://metalk-a52fb.web.app/post/$postId"

                  val intent = Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(Intent.EXTRA_TEXT, link)
                  }
                  startActivity(Intent.createChooser(intent, "Chia sẻ"))
              }

              override fun onItemClickSave(position: Int) {
                  Toast.makeText(this@SearchPostActivity, "CHưa làm gì cả", Toast.LENGTH_SHORT).show()
              }
          })

        searchHistoryAdapter.setOnItemClickListener(object : SearchHistoryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val searchQuery = allSearchHistory?.get(position)?.content ?: return
                binding.toolbarLayout.etSearch.setText(searchQuery)
                binding.toolbarLayout.etSearch.setSelection(searchQuery.length)
            }

            override fun onItemClickDelete(position: Int) {
                val searchHistory = allSearchHistory?.get(position) ?: return
                searchHistoryViewModel.deleteSearchHistory(searchHistory,onSuccess = {
                    searchHistoryAdapter.removeData(searchHistory)


                    allSearchHistory = allSearchHistory?.toMutableList()?.apply {
                        removeAt(position)
                    }
                })
            }
        })
    }

    //query data search
    private fun queryData() {

        searchTextWatcher?.let {binding.toolbarLayout.etSearch.removeTextChangedListener(it) }

        searchTextWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?)= Unit

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) = Unit

            override fun onTextChanged(
                char: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                if (char.isNullOrEmpty()) {
                    postAdapter.clearData()
                    binding.gChip.visibility = View.GONE
                    binding.rvPost.visibility = View.GONE
                    binding.rvRecently.visibility = View.VISIBLE
                    binding.tvNoResult.visibility = View.GONE
                    return
                }
                binding.gChip.visibility = View.VISIBLE
                binding.rvPost.visibility = View.VISIBLE
                binding.rvRecently.visibility = View.GONE
                val filteredPosts = allPost?.filter { post ->
                    post.title.contains(char.toString(), ignoreCase = true) ||
                            post.description.contains(char.toString(), ignoreCase = true)
                }
                filteredPosts?.let {
                    postAdapter.clearData()
                    postAdapter.addNewData(it)

                    if (it.isEmpty()) {
                        binding.tvNoResult.visibility = View.VISIBLE
                    } else {
                        binding.tvNoResult.visibility = View.GONE
                    }

                    binding.rvPost.visibility = View.VISIBLE
                    setOnClick(it)
                }
            }
        }
        searchTextWatcher?.let { binding.toolbarLayout.etSearch.addTextChangedListener(it) }
    }


    //lấy data từ viewmodel
    private fun getDataFromViewModel() {
        postViewModel.postsShared.observe(this) { posts ->
            allPost = posts
        }
        searchHistoryViewModel.searchHistory.observe(this) { searchHistory ->
            allSearchHistory = searchHistory
            searchHistoryAdapter.addNewData(allSearchHistory ?: emptyList())
        }
    }
    //thiết lập toolbar cho màn hình SreachPost
    private fun setUpToolbar() {
        setupToolbarCus(
            toolbar = binding.toolbarLayout.toolbar,
            title = getString(R.string.search_post_title),
            isShowSearch = true,
            isBack = true,
            onBack = {
                setKeybroad()
            }
        )
    }
}