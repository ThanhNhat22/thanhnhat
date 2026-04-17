package com.app.findback.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.findback.R
import com.app.findback.databinding.FragmentMapBinding
import com.app.findback.domain.repositories.model.Post
import com.app.findback.ui.components.bottom_sheet.MapBottomSheet
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.PostViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.BoundingBox
class MapFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            //xin quyền truy cập vị trí
            if (isGranted && hasLocationPermission()) {
//                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Vui lòng cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
            }
        }
    private lateinit var map: MapView
    //lấy vị trí hiện tại
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private val postMarkers = mutableListOf<Marker>()
    val vietnamBounds = BoundingBox(
        23.5,   //vĩ độ Bắc
        109.5,  // kinh độ Đông
        8.5,    // South
        102.0   // West
    )
    //viewmodel post (shared với Activity)
    private lateinit var postViewModel: PostViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl()
        setupMap()
        setPermission()
        showLocationOfPosts()
        postViewModel.getPosts()
        ferform()
    }
    private fun setControl(){
        //lay61 vị trí thật của người dùng
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]
    }

    private fun showLocationOfPosts() {
        postViewModel.postsShared.observe(viewLifecycleOwner) { posts ->
            showMarkers(requireContext(), posts)
        }
    }
    //hàm lấy vị trí hiện tại
    private fun ferform(){
        binding.btnMyLocation.setOnClickListener { getCurrentLocation() }
    }
    //lấy vị trí hiện tại
    private fun getCurrentLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        try {
            //lấy vị cuối cùng của người dùng
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        showCurrentLocation(loc)
                    }
                }
        } catch (_: SecurityException) {
            Toast.makeText(requireContext(), "Không lấy được vị trí", Toast.LENGTH_SHORT).show()
        }
    }

    //kiểm tra quyền truy cập vị trí
    private fun hasLocationPermission(): Boolean {
        //kiểm tra quyền truy cập vị trí
        //fine là vị trí chích xác cao
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
         //coarse là vị trí gần đúng
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    //show vị trí hiện tại lên map
    private fun showCurrentLocation(location: Location) {
        //tao vĩ độ và kinh dộ
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        // move map tới vị trí
        map.controller.setCenter(geoPoint)
        // chỉ thay marker vị trí hiện tại, không xóa list marker khác
        currentLocationMarker?.let { map.overlays.remove(it) }
        // thêm marker
        val marker = Marker(map)
        marker.position = geoPoint
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.title = "Vị trí của bạn"
        currentLocationMarker = marker
        map.overlays.add(marker)
        map.invalidate()
    }

    //set quyền và cấu hình osmdroid
    private fun setPermission() {
        //xin quyền truy cập vị trí
        if (hasLocationPermission()) {
            //lấy vị trí hiện tại
            getCurrentLocation()
        } else {
            //xin quyền lại
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        //cấu hỉnh cho osmdroid
        Configuration.getInstance().userAgentValue = requireContext().packageName
    }

    private fun setupMap() {
        map = binding.map

        //style app
        map.setTileSource(TileSourceFactory.MAPNIK)
        //set chỉ zoom trong việt nam
        map.setScrollableAreaLimitDouble(vietnamBounds)
        //zoom bẳng 2 ngón
        map.setMultiTouchControls(true)
        //zoom max
        map.maxZoomLevel = 25.0
        map.minZoomLevel = 6.5


        val controller = map.controller
        //zoom ở mức 15
        controller.setZoom(10.0)
    }
    //vẽ các địa điểm
    private fun showMarkers(context: Context, posts: List<Post>) {
        // clear marker cũ trước khi vẽ lại
        postMarkers.forEach { map.overlays.remove(it) }
        postMarkers.clear()
        posts.forEach { post ->
            if ( post.postType == "found" && post.status == "active" && post.latitude != 0.0 && post.longitude != 0.0) {
                val geoPoint = GeoPoint(post.latitude, post.longitude)
                val marker = Marker(map)
                marker.position = geoPoint
                marker.icon = ContextCompat.getDrawable(context, R.drawable.ic_location_green)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.setOnMarkerClickListener { _, _ ->
                    val sheet = MapBottomSheet(post)
                    sheet.show(requireActivity().supportFragmentManager, "MapSheet")
                    true
                }
                map.overlays.add(marker)
                postMarkers.add(marker)
            }
            else {
                val geoPoint = GeoPoint(post.latitude, post.longitude)
                val marker = Marker(map)
                marker.position = geoPoint
                marker.icon = ContextCompat.getDrawable(context, R.drawable.ic_location_red)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.setOnMarkerClickListener { _, _ ->
                    val sheet = MapBottomSheet(post)
                    sheet.show(requireActivity().supportFragmentManager, "MapSheet")
                    true
                }
                map.overlays.add(marker)
                postMarkers.add(marker)
            }

        }

        map.invalidate()
    }
    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding.map.overlays.clear()
        _binding = null
        super.onDestroyView()
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            backgroudResId = R.color.transparent,
            titleResId = R.string.nav_map,
            isBack = false,
            ib1Res = R.drawable.ic_search
        )
    }
}