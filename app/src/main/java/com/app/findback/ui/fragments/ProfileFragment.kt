package com.app.findback.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.LoginActivity
import com.app.findback.R
import com.app.findback.databinding.FragmentProfileBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.adapters.MyPostAdapter
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MyPostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadUserInfo()
        loadMyPosts()
        setupActions()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = MyPostAdapter(postList)
        binding.rvMyPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProfileFragment.adapter
        }
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        binding.txtName.text = user?.displayName ?: "Người dùng"
        binding.txtEmail.text = user?.email ?: ""
    }

    private fun loadMyPosts() {
        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("posts")
            .orderByChild("userId")
            .equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()

                    for (data in snapshot.children) {
                        try {
                            val post = data.getValue(Post::class.java)
                            post?.let { postList.add(it) }
                        } catch (e: Exception) {
                            // In ra lỗi cụ thể để biết field nào đang gây crash
                            android.util.Log.e("ProfileFragment",
                                "Parse post ${data.key} thất bại: ${e.message}", e)
                        }
                    }

                    binding.txtPostCount.text = postList.size.toString()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("ProfileFragment", "Database error: ${error.message}")
                }
            })
    }

    private fun setupActions() {
        binding.btnEditProfile.setOnClickListener {
            showToast("Chức năng đang phát triển")
        }

        binding.btnLogout.setOnClickListener {
            safeLogout()
        }
    }

    private fun safeLogout() {
        try {
            auth.signOut()

            // Delay nhỏ để Firebase xử lý xong sign out
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finishAffinity()   // Đóng tất cả activity
            }, 400)

        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().finishAffinity()
        }
    }

    private fun showToast(message: String) {
        if (!isAdded || requireActivity().isFinishing || requireActivity().isDestroyed) return
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            titleResId = R.string.nav_profile,
            isBack = false,
            isShowSearch = false
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}