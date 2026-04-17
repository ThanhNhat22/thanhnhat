package com.app.findback.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.TextWatcher
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.findback.R
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.app.findback.databinding.FragmentMapBinding
import com.app.findback.domain.repositories.model.Post
import com.app.findback.ui.components.bottom_sheet.MapBottomSheet
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.PostViewModel
import com.app.findback.utils.extentions.ConvertTime
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.android.gms.location.FusedLocationProviderClient
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.Polygon

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
    private var isShowSearch = false
    private var iconIB1 = R.drawable.ic_search
    private var allPosts: List<Post> = emptyList()
    private var currentQuery: String = ""
    private var searchTextWatcher: TextWatcher? = null
    private var totalFound = 0
    private var totalLost = 0
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
        observeSearchLive()
        postViewModel.getPosts()
        ferform()
    }
    private fun setControl(){
        //lay61 vị trí thật của người dùng
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]
    }
    //lọc tìm kiếm theo tên tiêu đề của post và zoom đến đó
    private fun filterPosts(query: String) {
        currentQuery = query
        val keyword = query.trim().lowercase()
        val filteredPosts = if (keyword.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.title.lowercase().contains(keyword) ||
                    post.locationText.lowercase().contains(keyword)
            }
        }

        showMarkers(requireContext(), filteredPosts)
        if (keyword.isNotEmpty()) {
            zoomToFirstMatch(filteredPosts)
        }
    }

    private fun observeSearchLive() {
        val searchInput = (activity as? BaseBottomNavActivity)?.getToolbarSearchInput() ?: return
        searchTextWatcher?.let { searchInput.removeTextChangedListener(it) }

        searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPosts(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        }
        searchInput.addTextChangedListener(searchTextWatcher)
    }

    //zoom đến vị trí được tìm thấy đầu tiên
    private fun zoomToFirstMatch(posts: List<Post>) {
        val firstValid = posts.firstOrNull { hasValidLocation(it) } ?: return
        val target = GeoPoint(firstValid.latitude, firstValid.longitude)
        map.controller.animateTo(target)
        if ((map.zoomLevelDouble) < 15.0) {
            map.controller.setZoom(15.0)
        }
    }

    //kiểm tra vị trí có hợp lệ
    private fun hasValidLocation(post: Post): Boolean {
        return post.latitude != 0.0 && post.longitude != 0.0
    }
    //hiện tất cả vị trí post và filter theo tên
    private fun showLocationOfPosts() {
        postViewModel.postsShared.observe(viewLifecycleOwner) { posts ->
            allPosts = posts
            filterPosts(currentQuery)
        }
    }
    //hàm lấy vị trí hiện tại
    private fun ferform(){
        binding.btnMyLocation.setOnClickListener { getCurrentLocation() }
    }
    //set sự kiên bấm nút search
    private fun setEventClickSearch()  {
           isShowSearch = !isShowSearch
           iconIB1 = if (isShowSearch) R.drawable.ic_close else R.drawable.ic_search
           (activity as? BaseBottomNavActivity)?.refreshToolbarForActiveFragment()

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
               filterPosts(currentQuery)
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
        //vẽ vùng tròn
        drawCircle(map, location.latitude, location.longitude, 1000.0)
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
            if (!hasValidLocation(post)) return@forEach
            if ( post.postType == "found" && post.status == "active") {
                val geoPoint = GeoPoint(post.latitude, post.longitude)
                val marker = Marker(map)
                marker.position = geoPoint

                marker.icon = createMarkerTextTopIconBottom(
                    context,
                    ConvertTime.formatTime(post.incidentDatetime),
                    R.drawable.ic_location_green
                )
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
                marker.icon = createMarkerTextTopIconBottom(
                    context,
                    ConvertTime.formatTime(post.incidentDatetime),
                    R.drawable.ic_location_red
                )
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
        binding.textTotalFound.text = posts.filter { it.postType == "found" && it.status == "active" }.size.toString()
        binding.textTotalLost.text = posts.filter { it.postType == "lost" && it.status == "active"}.size.toString()
        map.invalidate()
    }
    //vẽ vùng tròn
    fun drawCircle(
        mapView: MapView,
        lat: Double,
        lon: Double,
        radiusMeters: Double
    ) {

        val center = GeoPoint(lat, lon)

        val circle = Polygon(mapView)

        circle.points = Polygon.pointsAsCircle(center, radiusMeters)

        circle.fillColor = 0x3300FF00   // xanh trong suốt
        circle.strokeColor = Color.RED
        circle.strokeWidth = 3f

        mapView.overlays.add(circle)
        mapView.invalidate()
    }
    //custom marker
    fun createMarkerTextTopIconBottom(
        context: Context,
        text: String,
        iconRes: Int
    ): BitmapDrawable {

        val width = 220
        val height = 200

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // text
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val rect = RectF(10f, 10f, width - 10f, 70f)
        canvas.drawRoundRect(rect, 20f, 20f, bgPaint)

        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)

        canvas.drawText(text, width / 2f, 55f, textPaint)

        // icon
        val icon = ContextCompat.getDrawable(context, iconRes)
        icon?.setBounds(60, 80, 160, 180)
        icon?.draw(canvas)

        return BitmapDrawable(context.resources, bitmap)
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
        (activity as? BaseBottomNavActivity)?.getToolbarSearchInput()?.let { editText ->
            searchTextWatcher?.let { watcher -> editText.removeTextChangedListener(watcher) }
        }
        searchTextWatcher = null
        isShowSearch = false
        binding.map.overlays.clear()
        _binding = null
        super.onDestroyView()
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            backgroudResId = R.color.transparent,
            titleResId = R.string.nav_map,
            isBack = false,
            isShowSearch = isShowSearch,
            ib1Res = iconIB1,
            onIB1 = { setEventClickSearch() }
        )
    }
}