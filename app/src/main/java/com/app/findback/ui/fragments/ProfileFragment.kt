package com.app.findback.ui.fragments

import android.content.Intent
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MyPostAdapter

    private val postList = mutableListOf<Post>()

    // Firebase listener
    private var postsListener: ValueEventListener? = null
    private lateinit var postsRef: Query

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadUserInfo()
        loadMyPosts()
        setupActions()

        return binding.root
    }

    private fun setupRecyclerView() {

        adapter = MyPostAdapter(
            list = postList,
            isMyPost = true,

            onEdit = { post ->

                val bottomSheet =
                    EditPostBottomSheet.newInstance(post)

                bottomSheet.setOnDismissListener {
                    // Firebase realtime tự cập nhật
                }

                bottomSheet.show(
                    parentFragmentManager,
                    "EditPost"
                )
            },

            onDelete = { post ->

                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa bài đăng này không?")
                    .setCancelable(false)

                    .setPositiveButton("Xóa") { _, _ ->

                        FirebaseDatabase.getInstance()
                            .getReference("posts")
                            .child(post.postId)
                            .removeValue()

                            .addOnSuccessListener {

                                if (_binding == null) return@addOnSuccessListener

                                Toast.makeText(
                                    requireContext(),
                                    "Đã xóa bài đăng",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            .addOnFailureListener {

                                if (_binding == null) return@addOnFailureListener

                                Toast.makeText(
                                    requireContext(),
                                    "Xóa thất bại: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                    .setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }

                    .show()
            }
        )

        binding.rvMyPosts.apply {

            layoutManager =
                LinearLayoutManager(requireContext())

            adapter = this@ProfileFragment.adapter
        }
    }

    private fun loadUserInfo() {

        val user = auth.currentUser ?: return

        binding.txtEmail.text =
            user.email ?: ""

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)
            .get()

            .addOnSuccessListener { snapshot ->

                if (_binding == null) return@addOnSuccessListener

                val fullName =
                    snapshot.child("fullName")
                        .value?.toString()
                        ?: "Người dùng"

                val avatar =
                    snapshot.child("avatar")
                        .value?.toString()
                        ?: ""

                binding.txtName.text = fullName

                if (avatar.isNotEmpty()) {

                    Glide.with(requireContext())
                        .load(avatar)
                        .placeholder(R.drawable.logo_tran)
                        .circleCrop()
                        .into(binding.imgAvatar)
                }
            }

            .addOnFailureListener {

                if (_binding == null) return@addOnFailureListener

                binding.txtName.text =
                    user.displayName ?: "Người dùng"
            }
    }

    private fun loadMyPosts() {

        val uid = auth.currentUser?.uid ?: return

        postsRef = FirebaseDatabase.getInstance()
            .getReference("posts")
            .orderByChild("userId")
            .equalTo(uid)

        postsListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                postList.clear()

                for (data in snapshot.children) {

                    try {

                        val map =
                            data.value as? Map<String, Any?>
                                ?: continue

                        val post = Post.fromMap(map)

                        postList.add(post)

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                // Bài mới nhất lên đầu
                postList.sortByDescending {
                    it.createdAt
                }

                binding.txtPostCount.text =
                    postList.size.toString()

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        postsRef.addValueEventListener(postsListener!!)
    }

    private fun setupActions() {

        binding.btnEditProfile.setOnClickListener {

            val bottomSheet =
                EditProfileBottomSheet()

            bottomSheet.setOnDismissListener {
                loadUserInfo()
            }

            bottomSheet.show(
                parentFragmentManager,
                "EditProfileBottomSheet"
            )
        }

        binding.btnLogout.setOnClickListener {

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn chắc chắn muốn đăng xuất?")

                .setPositiveButton("Đăng xuất") { _, _ ->

                    auth.signOut()

                    if (_binding == null) return@setPositiveButton

                    Toast.makeText(
                        requireContext(),
                        "Đăng xuất thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(
                        requireContext(),
                        LoginActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    requireActivity().finish()
                }

                .setNegativeButton("Hủy", null)

                .show()
        }
    }

    override fun toolbarConfig() = ToolbarConfig(
        titleResId = R.string.nav_profile,
        isBack = false,
        isShowSearch = false
    )

    override fun onDestroyView() {

        postsListener?.let {
            postsRef.removeEventListener(it)
        }

        _binding = null

        super.onDestroyView()
    }
}