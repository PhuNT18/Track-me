package com.phunt.trackme.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.phunt.trackme.R
import com.phunt.trackme.adapters.TrackingHistoryAdapter
import com.phunt.trackme.databinding.FragmentHistoryTrackingBinding
import com.phunt.trackme.viewmodels.TrackingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class HistoryTrackingFragment : Fragment() {
    private var TAG: String = HistoryTrackingFragment::class.java.simpleName
    private lateinit var binding: FragmentHistoryTrackingBinding
    private val viewModel by viewModels<TrackingViewModel>()
    private val adapter = TrackingHistoryAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHistoryTrackingBinding.inflate(inflater, container, false)

        // Set the adapter
        binding.rcvHistoryTracking.setHasFixedSize(true)
        binding.rcvHistoryTracking.adapter = adapter
        binding.rcvHistoryTracking.setRecyclerListener(mRecycleListener)
        initEvent()
        return binding.root
    }

    private fun initEvent(){
        lifecycleScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            viewModel.allTrackings.collectLatest { adapter.submitData(it) }
        }
        binding.imgRecord.setOnClickListener {
            findNavController().navigate(R.id.action_history_to_tracking)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "requestCode: $requestCode, grantResult: $grantResults")
    }

    /**
     * RecycleListener that completely clears the [com.google.android.gms.maps.GoogleMap]
     * attached to a row in the RecyclerView.
     * Sets the map type to [com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE] and clears
     * the map.
     */
    private val mRecycleListener = RecyclerView.RecyclerListener { holder ->
        val mapHolder: TrackingHistoryAdapter.TrackingViewHolder = holder as  TrackingHistoryAdapter.TrackingViewHolder
        // Clear the map and free up resources by changing the map type to none.
        // Also reset the map when it gets reattached to layout, so the previous map would
        // not be displayed.
        mapHolder.map?.clear()
        mapHolder.map?.mapType = GoogleMap.MAP_TYPE_NONE
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                HistoryTrackingFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}