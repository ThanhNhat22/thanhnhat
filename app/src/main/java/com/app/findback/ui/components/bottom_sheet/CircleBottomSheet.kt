package com.app.findback.ui.components.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.app.findback.databinding.LayoutCircleBottomSheetBinding
import com.app.findback.domain.models.CircleZone
import com.app.findback.ui.viewmodel.CircleZoneViewModel

class CircleBottomSheet(private val circleZone: CircleZone) : BaseBottomSheet(){
    private var _binding: LayoutCircleBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var circleZoneViewModel: CircleZoneViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutCircleBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl()
        setEvent()
    }
    //set event
    private fun setEvent() {
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        binding.edtName.clearFocus()
        binding.edtName.setSelection(binding.edtName.text.length ?: 0)

        binding.btnDelete.setOnClickListener {
            deleteCircleZone(circleZone = circleZone)
        }
        binding.btnSave.setOnClickListener {
            onSave()
        }
    }
    //sự kiện lưu
    private fun onSave() {
        val name = binding.edtName.text.toString()
        val radius = binding.edtRadius.text.toString().toDoubleOrNull()
        if (name.isEmpty() || radius == null) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        if (radius > 1000){
            Toast.makeText(requireContext(), "Vui lòng nhập bán kính nhỏ hơn 2000m", Toast.LENGTH_SHORT).show()
            return
        }
        val newCircleZone = circleZone.copy(name = name, radius = radius)
        updateCircleZone(circleZone = newCircleZone)
    }
    //set control
    private fun setControl(){
        circleZoneViewModel = ViewModelProvider(requireActivity())[CircleZoneViewModel::class.java]
        binding.edtName.setText(circleZone.name)
        binding.edtRadius.setText(circleZone.radius.toString())
        binding.tvLatLng.text = "Tọa độ: ${circleZone.centerLat} - ${circleZone.centerLon}"
    }
    //datele circle zone
    private fun deleteCircleZone(userId: String = "1234", circleZone: CircleZone) {
        val ctx = context ?: return@deleteCircleZone
        circleZoneViewModel.deleteCircleZone(onSuccess = {
            Toast.makeText(ctx, "Xóa vùng tròn thành công", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }, userId = userId, circleZone = circleZone)
    }
    //update
    private fun updateCircleZone(userId: String = "1234", circleZone: CircleZone) {
        val ctx = context ?: return@updateCircleZone
        circleZoneViewModel.updateCircleZone(onSuccess = {
            Toast.makeText(ctx, "Cập nhật vùng tròn thành công", Toast.LENGTH_SHORT).show()
           dismissAllowingStateLoss()
        }, userId = userId, circleZone = circleZone)
    }
}