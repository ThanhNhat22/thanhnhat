package com.app.findback.ui.components.bottom_sheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.findback.R
import com.app.findback.databinding.LayoutMapBottomSheetBinding
import com.app.findback.domain.models.Post
import com.app.findback.utils.extentions.ConvertTime

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        setEvent()
    }
    //set evetnt
    private fun setEvent(){
        binding.btnViewInGoogle.setOnClickListener {
            openGoogleMap(post.latitude, post.longitude)
        }
    }
    //set dữ liệu
    private fun setData(){
        if (post.postType == "lost") {
            binding.textType.text = "Thất lạc"
            binding.textType.setTextColor(resources.getColor(R.color.primary_red))
        } else {
            binding.textType.text = "Tìm thấy"
            binding.textType.setTextColor(resources.getColor(R.color.primary_green))
        }
        binding.textTitle.text = post.title
        binding.textLocation.text = post.locationText
        binding.textDescription.text = post.description
        binding.textDatetime.text = ConvertTime.formatTime(post.incidentDatetime)
    }
    //open bằng gg map
    fun openGoogleMap(lat: Double, lng: Double) {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        val  context = requireContext()
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

     override fun onDestroyView() {
         super.onDestroyView()
         _binding = null
     }


}