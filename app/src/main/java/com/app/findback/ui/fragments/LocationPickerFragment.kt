package com.app.findback.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.app.findback.R
import com.app.findback.databinding.FragmentLocationPickerBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class LocationPickerFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentLocationPickerBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null

    private var currentLat = 10.7769
    private var currentLng = 106.7009

    // =========================================================
    // LOCATION PERMISSION
    // =========================================================

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                moveToMyLocation()
            } else {
                toast("Bạn chưa cấp quyền vị trí")
            }
        }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLocationPickerBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)

        binding.btnMyLocation.setOnClickListener {
            checkLocationPermission()
        }

        binding.btnCircle.setOnClickListener {
            googleMap?.animateCamera(
                CameraUpdateFactory.zoomTo(15f)
            )
        }

        binding.btnConfirmLocation.setOnClickListener {
            returnResult()
        }
    }

    // =========================================================
    // MAP READY
    // =========================================================

    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        val defaultLocation = LatLng(currentLat, currentLng)

        placeMarker(defaultLocation)

        map.setOnMapClickListener { latLng ->
            placeMarker(latLng)
        }
    }

    // =========================================================
    // PLACE MARKER
    // =========================================================

    private fun placeMarker(latLng: LatLng) {

        currentLat = latLng.latitude
        currentLng = latLng.longitude

        marker?.remove()

        marker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Vị trí đã chọn")
                .draggable(true)
        )

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 16f)
        )

        // Hiển thị địa chỉ
        val address = getAddress(currentLat, currentLng)

        binding.tvSelectedLocation.text = address

        // Hiện nút xác nhận
        binding.btnConfirmLocation.visibility = View.VISIBLE
    }

    // =========================================================
    // CHECK PERMISSION
    // =========================================================

    private fun checkLocationPermission() {

        when {

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                moveToMyLocation()
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    // =========================================================
    // MOVE TO MY LOCATION
    // =========================================================

    private fun moveToMyLocation() {

        val client =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        client.lastLocation.addOnSuccessListener { location ->

            location?.let {

                val latLng = LatLng(
                    it.latitude,
                    it.longitude
                )

                placeMarker(latLng)
            }
        }
    }

    // =========================================================
    // RETURN RESULT
    // =========================================================

    private fun returnResult() {

        val address = getAddress(currentLat, currentLng)

        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                ARG_LAT to currentLat,
                ARG_LNG to currentLng,
                ARG_ADDRESS to address
            )
        )

        parentFragmentManager.popBackStack()
    }

    // =========================================================
    // GET ADDRESS
    // =========================================================

    private fun getAddress(
        lat: Double,
        lng: Double
    ): String {

        return try {

            val geocoder = Geocoder(
                requireContext(),
                Locale.getDefault()
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                "$lat, $lng"

            } else {

                val addresses =
                    geocoder.getFromLocation(lat, lng, 1)

                addresses?.firstOrNull()?.getAddressLine(0)
                    ?: "$lat, $lng"
            }

        } catch (e: Exception) {

            "$lat, $lng"
        }
    }

    // =========================================================

    private fun toast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // =========================================================
    // COMPANION
    // =========================================================

    companion object {

        const val REQUEST_KEY = "location_picker_result"

        const val ARG_LAT = "lat"

        const val ARG_LNG = "lng"

        const val ARG_ADDRESS = "address"
    }
}