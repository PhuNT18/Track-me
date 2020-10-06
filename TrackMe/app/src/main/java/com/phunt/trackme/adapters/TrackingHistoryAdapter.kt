package com.phunt.trackme.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.phunt.trackme.MainApplication
import com.phunt.trackme.databinding.TrackingHistoryRowBinding
import com.phunt.trackme.db.entity.TrackingEntity

class TrackingHistoryAdapter: PagingDataAdapter<TrackingEntity, TrackingHistoryAdapter.TrackingViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingViewHolder {
        return TrackingViewHolder(TrackingHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TrackingViewHolder, position: Int) {
        val record = getItem(position)
        if (record != null) {
            holder.bind(record)
        }
    }


    class TrackingViewHolder(
            private val binding: TrackingHistoryRowBinding,
            var map: GoogleMap? = null,
            private var points: MutableList<LatLng>? = null
    ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {
        init {
            binding.mapLite.onCreate(null)
            binding.mapLite.getMapAsync(this)
        }

        fun bind(item: TrackingEntity) {
            binding.apply {
                record = item
                val t: Long = item.time
                val h: Int = (((t / (60 * 60)) % 24).toInt())
                val m: Int = (t / 60 % 60).toInt()
                val s: Int = (t % 60).toInt()
                binding.tvTimer.text = String.format("%02d:%02d:%02d", h, m, s)
                points = PolyUtil.decode(item.polyLine)
                drawPolyline()
            }
        }

        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(MainApplication.instance)
            map = googleMap
            map!!.uiSettings.setAllGesturesEnabled(false)
            binding.mapLite.isClickable = false
            drawPolyline()
        }

        private fun drawPolyline() {
            if (map == null) return
            var mutablePolyline: Polyline? = map?.addPolyline(PolylineOptions()
                    .color(Color.CYAN)
                    .width(4f))
            mutablePolyline?.points = points

            map?.addMarker(MarkerOptions()
                    .position(points!![0])
                    .title("Start location")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)))
            map?.addMarker(MarkerOptions()
                    .position(points!![points!!.size - 1])
                    .title("End location"))

            //Handle start/stop location bounds center on screen
            val builder: LatLngBounds.Builder = LatLngBounds.Builder()
            points!!.forEach {
                builder.include(it)
            }

            val bounds = builder.build()
            val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)
            map!!.animateCamera(cu);

            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    /**
     * Location represented by a position ([com.google.android.gms.maps.model.LatLng] and a
     * name ([java.lang.String]).
     */
    class NamedLocation(val name: String, val location: LatLng)

    companion object {
        /**
         * This diff callback informs the PagedListAdapter how to compute list differences when new
         * PagedLists arrive.
         * <p>
         * When you add a Cheese with the 'Add' button, the PagedListAdapter uses diffCallback to
         * detect there's only a single item difference from before, so it only needs to animate and
         * rebind a single view.
         *
         * @see DiffUtil
         */
        private val diffCallback = object : DiffUtil.ItemCallback<TrackingEntity>() {
            override fun areItemsTheSame(oldItem: TrackingEntity, newItem: TrackingEntity): Boolean =
                    oldItem.id == newItem.id

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: TrackingEntity, newItem: TrackingEntity): Boolean =
                    oldItem == newItem
        }
    }
}