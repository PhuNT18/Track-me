package com.phunt.trackme.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.phunt.trackme.R
import com.phunt.trackme.databinding.FragmentTrackingBinding
import com.phunt.trackme.db.entity.TrackingEntity
import com.phunt.trackme.tracker.LocationTracker
import com.phunt.trackme.tracker.ProviderError
import com.phunt.trackme.viewmodels.TrackingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Duration
import java.util.*

class TrackingFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private var TAG: String = TrackingFragment::class.java.simpleName
    private val viewModel by viewModels<TrackingViewModel>()
    private lateinit var binding: FragmentTrackingBinding
    private var map: GoogleMap? = null
    private var mutablePolyline: Polyline? = null

    // Last known location
    private var lastKnownLocation: LatLng? = null
    private var oldLocation: Location? = null //User for calculate speed
    private var points: MutableList<LatLng>? = null
    private var startMarker: Marker? = null
    private var stopMarker: Marker? = null
    private var drawJob: Job? = null
    private var isPauseTracking: Boolean = false
    private var distance: Float = 0f // Km
    private var speed: Float = 0f // Km/h
    private var time: Long = 0 // second
    private var tracker: LocationTracker = LocationTracker(
            minTimeBetweenUpdates = 2000L, // 2 second
            minDistanceBetweenUpdates = 3F, // 3 meter
            shouldUseGPS = true,
            shouldUseNetwork = true,
            shouldUsePassive = true
    ).also {
        it.addListener(object : LocationTracker.Listener {
            override fun onLocationFound(location: Location) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (!isPauseTracking) {
                    calculateSpeed(location)
                }
                lastKnownLocation = latLng
                oldLocation = location
                addMarker(true)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                updateTrack()
            }

            override fun onProviderError(providerError: ProviderError) {
            }

        })
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        mutablePolyline = map?.addPolyline(PolylineOptions()
                .color(Color.CYAN)
                .width(4f))
        startStop()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        initUi()
        initEvent()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        startStop()
    }

    private fun initUi() {
        binding.tvDistance.text = String.format(getString(R.string.distance), 0.0f)
        binding.tvSpeed.text = String.format(getString(R.string.speed), 0f)
        binding.tvTimer.setOnChronometerTickListener {
            lifecycleScope.launch {
                val t: Long = SystemClock.elapsedRealtime() - it.base
                val h: Int = (((t / (1000 * 60 * 60)) % 24).toInt())
                val m: Int = (t / (1000 * 60) % 60).toInt()
                val s: Int = ((t / 1000) % 60).toInt()
                time = t/1000
                it.text = String.format("%02d:%02d:%02d", h, m, s)
            }
        }
        binding.tvTimer.base = SystemClock.elapsedRealtime()
        binding.tvTimer.start()
    }

    private fun initEvent() {
        binding.imgActionPause.setOnClickListener {
            isPauseTracking = true
            speed = 0f
            binding.tvSpeed.text = String.format(resources.getString(R.string.speed), speed)
            binding.tvTimer.stop()
            binding.imgActionPause.visibility = View.INVISIBLE
            binding.linearContainActions.visibility = View.VISIBLE
            addMarker(false)
        }

        binding.imgActionStop.setOnClickListener {
            storeDB()
            activity?.onBackPressed()
        }

        binding.imgActionReRecord.setOnClickListener {
            map?.clear()
            time = 0
            binding.tvTimer.text = String.format("%02d:%02d:%02d", 0, 0, 0)
            binding.tvTimer.base = SystemClock.elapsedRealtime()
            binding.tvTimer.start()
            isPauseTracking = false
            startMarker = null
            points = null
            mutablePolyline = map?.addPolyline(PolylineOptions()
                    .color(Color.CYAN)
                    .width(4f))

            binding.tvDistance.text = String.format(getString(R.string.distance), 0f)
            addMarker(true)
            binding.imgActionPause.visibility = View.VISIBLE
            binding.linearContainActions.visibility = View.INVISIBLE
        }
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(context, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    //Draw polyline when tracking
    private fun updateTrack() {
        drawJob?.cancel()
        if (isPauseTracking) return
        drawJob = lifecycleScope.launch {
            points = mutablePolyline?.points as MutableList<LatLng>
            lastKnownLocation?.let { points?.add(it) }
            mutablePolyline?.points = points
            distance = (SphericalUtil.computeLength(points) / 1000).toFloat()
            distance = formatFloat(distance)
            binding.tvDistance.text = String.format(getString(R.string.distance), distance)
        }
    }

    private fun addMarker(isStartMarker: Boolean) {
        if (isStartMarker) {
            if (lastKnownLocation != null && startMarker == null) {
                startMarker = map?.addMarker(MarkerOptions()
                        .position(lastKnownLocation!!)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_AZURE)))
            }
        } else {
            if (lastKnownLocation != null) {
                stopMarker = map?.addMarker(MarkerOptions()
                        .position(lastKnownLocation!!)
                        .title("Start location"))
            }
        }
    }

    private fun calculateSpeed(currentLocation: Location) {
        if(oldLocation != null) {
            Log.d(TAG, "Old location: $lastKnownLocation, Current location: (${currentLocation.latitude},${currentLocation.longitude})")
            val difTime = (currentLocation.elapsedRealtimeNanos - oldLocation!!.elapsedRealtimeNanos)/(1000000*1000) //in millisecond
            val distance2Point = (SphericalUtil.computeDistanceBetween(lastKnownLocation, LatLng(currentLocation.latitude, currentLocation.longitude))).toFloat()
            if(difTime != 0L) {
                speed = (distance2Point / difTime)*3600/1000
            }
            Log.d(TAG, "Distance2Point: $distance2Point(m), DifTime: $difTime(s) - Calculate current Speed: $speed(Km/h)")
        }
        speed = formatFloat(speed)
        Log.d(TAG, "Current Speed: $speed(Km/h)")
        binding.tvSpeed.text = String.format(resources.getString(R.string.speed), speed)
    }

    //Start/Stop tracking
    @SuppressLint("MissingPermission")
    private fun startStop() {
        if (tracker.isListening) {
            tracker.stopListening()
            Log.d(TAG, "Stop tracking")
        } else {
            context?.let { tracker.startListening(it) }
            Log.d(TAG, "Start tracking")
        }
    }

    private fun storeDB(){
        lifecycleScope.launch {
            if(points != null && points!!.size > 0) {
                val polyLineStr = PolyUtil.encode(points)
                val avgSpeed = formatFloat((distance/time)*3600)
                val trackingRecord = TrackingEntity(polyLineStr, distance, avgSpeed, time, Date(System.currentTimeMillis()))
                Log.d(TAG, "storeDB: $trackingRecord")
                viewModel.insert(trackingRecord)
            }
        }
    }

    private fun formatFloat(number : Float) : Float{
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toFloat()
    }
}