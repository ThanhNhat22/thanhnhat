package com.app.findback.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.R
import com.app.findback.databinding.FragmentAddBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.adapters.ImagePickerAdapter
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

class AddStatusFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var postViewModel: PostViewModel
    private lateinit var imageAdapter: ImagePickerAdapter

    private val selectedImages = mutableListOf<Uri>()

    // 👇 Tọa độ được chọn từ map
    private var pickedLat: Double = 0.0
    private var pickedLng: Double = 0.0
    private var pickedAddress: String = ""

    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) imageAdapter.addImages(uris)
        }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]

        setupImagePicker()
        setupPostTypeSelector()
        setupListeners()
        observeUploadState()
        listenLocationPickerResult()   // 👈 nhận kết quả từ map picker
    }

    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private fun setupImagePicker() {
        imageAdapter = ImagePickerAdapter(
            selectedImages,
            onAddClick = { pickImagesLauncher.launch("image/*") },
            onRemoveClick = { position -> imageAdapter.removeImage(position) }
        )

        binding.rvImages.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = imageAdapter
        }
    }

    // =========================================================
    // POST TYPE SELECTOR
    // =========================================================

    private fun setupPostTypeSelector() {
        selectLost()
    }

    private fun selectLost() {
        binding.layoutLost.isSelected = true
        binding.layoutFound.isSelected = false
    }

    private fun selectFound() {
        binding.layoutLost.isSelected = false
        binding.layoutFound.isSelected = true
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private fun setupListeners() {
        binding.layoutLost.setOnClickListener { selectLost() }
        binding.layoutFound.setOnClickListener { selectFound() }
        binding.btnPost.setOnClickListener { uploadPost() }
        binding.btnCancel.setOnClickListener { resetForm() }

        binding.btnPickLocation.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LocationPickerFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // =========================================================
    // NHẬN KẾT QUẢ TỪ LOCATION PICKER
    // =========================================================

    private fun listenLocationPickerResult() {
        parentFragmentManager.setFragmentResultListener(
            LocationPickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            pickedLat     = bundle.getDouble(LocationPickerFragment.ARG_LAT)
            pickedLng     = bundle.getDouble(LocationPickerFragment.ARG_LNG)
            pickedAddress = bundle.getString(LocationPickerFragment.ARG_ADDRESS, "")

            // Tự điền địa chỉ vào ô text
            binding.edtLocation.setText(pickedAddress)

            // Hiển thị tọa độ nhỏ bên dưới (optional)
            binding.tvLocationCoords.text =
                "📍 ${"%.5f".format(pickedLat)}, ${"%.5f".format(pickedLng)}"
            binding.tvLocationCoords.visibility = View.VISIBLE
        }
    }

    // =========================================================
    // OBSERVE STATE
    // =========================================================

    private fun observeUploadState() {
        postViewModel.uploadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PostViewModel.UploadState.Loading -> {
                    binding.btnPost.isEnabled = false
                }
                is PostViewModel.UploadState.Success -> {
                    binding.btnPost.isEnabled = true
                    resetForm()
                    toast(getString(R.string.post_success))
                }
                is PostViewModel.UploadState.Error -> {
                    binding.btnPost.isEnabled = true
                    toast(state.message)
                }
            }
        }
    }

    // =========================================================
    // UPLOAD POST
    // =========================================================

    private fun uploadPost() {
        val title       = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val location    = binding.edtLocation.text.toString().trim()
        val postType    = if (binding.layoutLost.isSelected) "lost" else "found"

        if (title.isEmpty()) {
            toast(getString(R.string.error_empty_title)); return
        }
        if (description.isEmpty()) {
            toast(getString(R.string.error_empty_description)); return
        }
        if (location.isEmpty()) {
            toast(getString(R.string.error_empty_location)); return
        }
        if (pickedLat == 0.0 && pickedLng == 0.0) {
            toast("Vui lòng chọn vị trí trên bản đồ"); return
        }

        val now  = System.currentTimeMillis()
        val user = FirebaseAuth.getInstance().currentUser

        val imageUris = imageAdapter.getImages()

        val post = Post(
            postId       = now.toString(),
            userId       = user?.uid ?: "",
            userName     = user?.displayName ?: "Người dùng",
            userAvatar   = user?.photoUrl?.toString() ?: "",
            postType     = postType,
            title        = title,
            description  = description,
            locationText = location,
            latitude     = pickedLat,
            longitude    = pickedLng,
            createdAt    = now,
            updatedAt    = now,
            status       = "active"
        )

        postViewModel.uploadPost(post, imageUris)
    }

    // =========================================================
    // RESET FORM
    // =========================================================

    private fun resetForm() {
        binding.edtTitle.text?.clear()
        binding.edtDescription.text?.clear()
        binding.edtLocation.text?.clear()
        binding.tvLocationCoords.visibility = View.GONE

        pickedLat = 0.0
        pickedLng = 0.0
        pickedAddress = ""

        selectLost()
        imageAdapter.clear()
    }

    // =========================================================

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toolbarConfig() = ToolbarConfig(titleResId = R.string.post)
}