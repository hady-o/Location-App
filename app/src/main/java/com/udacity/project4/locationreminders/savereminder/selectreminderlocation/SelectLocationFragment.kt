package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.bind
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment :OnMapReadyCallback, BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map:GoogleMap
    private var myMarker: Marker? = null
    private val TAG = SelectLocationFragment::class.java.simpleName
    private val locationPermission = 5000
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.saveLocationButton.setOnClickListener {
            onLocationSelected()
        }


        return binding.root
    }


    private fun onLocationSelected() {

        myMarker?.let {
            _viewModel.reminderSelectedLocationStr.value = it.title
            _viewModel.latitude.value = it.position.latitude
            _viewModel.longitude.value = it.position.longitude
        }
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)

    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val zoom = 20f
        val syn = LatLng(-34.0,151.0)

            myMarker?.let {
                makeNewMarker(LatLng(it.position.latitude, it.position.longitude))
            }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(syn,zoom))
        getHome()
        map.setOnMapLongClickListener {
            makeNewMarker(it)
        }
        setPoiClick(map)
        StyleSetter(map)
    }
    private fun makeNewMarker(it: LatLng) {
        val snip = String.format(
            Locale.getDefault(),
            " lon%1$.5f,lat%2$.5f", it.longitude, it.latitude
        )
        myMarker?.remove()
        myMarker = map.addMarker(
            MarkerOptions().position(it).title("Dropped Pin").snippet(snip).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        )
    }



    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { po->
            myMarker!!.remove()
           val poi = map.addMarker(
                MarkerOptions()
                    .position(po.latLng)
                    .title(po.name)
            )
            myMarker = poi
            myMarker?.showInfoWindow()

        }
    }
    private fun StyleSetter(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_json
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun getHome() : Unit {
        if(ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        else

        {
                map.setMyLocationEnabled(true)
            val fus=
                LocationServices.getFusedLocationProviderClient(requireContext())
            val lastLocationTask = fus.lastLocation
            lastLocationTask.addOnCompleteListener(requireActivity()) {

                if (it.isSuccessful) {
                    val taskResult = it.result
                    taskResult?.run {


                            val latLng = LatLng(latitude, longitude)
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng,
                                    15f
                                )
                            )

                        makeNewMarker(latLng)
                    }
                }
            }

        }

    }





}

