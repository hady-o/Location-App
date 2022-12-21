package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var item: ReminderDataItem
    private lateinit var gClient: GeofencingClient

    private val allPerms = 33

    private val forOnlyPerms = 34

    private val locationOnRequest = 29
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value


            val forAppr = (
                    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                    )
            var backAppr = true
            if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                backAppr = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }

             item = ReminderDataItem(title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(item)) {
                if (forAppr && backAppr &&
                    _viewModel.validateEnteredData(item)) {
                    deviceChecking()
                } else {
                    getAllPrems(forAppr,backAppr)
                }
            }
//
        }
    }

    @TargetApi(29)
    private fun getAllPrems(forAppr:Boolean,backAppr:Boolean) {

        if (forAppr && backAppr)
            return
        var perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val res = when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q -> {
                perms += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                allPerms
            }
            else -> {
               forOnlyPerms
            }
        }
        requestPermissions(perms, res)

    }

    private fun deviceChecking(res: Boolean = true) {
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val myClient = LocationServices.getSettingsClient(requireActivity())
        val myBuilder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val locationSettingsResponseTask = myClient.checkLocationSettings(myBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && res) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        locationOnRequest,
                        null,
                        0,
                        0,
                        0,
                        null)
                } catch (sendEx: IntentSender.SendIntentException) {
                   Toast.makeText(requireContext(),sendEx.message,Toast.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(
                    requireView(),
                    "Please turn on your location", Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    deviceChecking()
                }.show()

            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                getGeo()
            }
        }
    }

    private fun getGeo() {
      val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
            PendingIntent.getBroadcast(
                requireContext(),
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        gClient = LocationServices.getGeofencingClient(requireActivity())
        val geo = Geofence.Builder()
            .setRequestId(item.id)
            .setCircularRegion(
                item.latitude!!,
                item.longitude!!,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()



        if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        gClient.addGeofences(GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geo)
            .build(), geofencePendingIntent).run {
            addOnSuccessListener {
                Log.i("geofencingClient", "Geofence is added successfully")
                _viewModel.saveReminder(item)
            }
            addOnFailureListener {
                _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
                _viewModel.showToast.value = it.message
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        _viewModel.showToast.value = "we Are in onActivityResult"
        if (requestCode == locationOnRequest) {
            deviceChecking()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult
                (requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED ||
            requestCode == allPerms &&
            grantResults[1] == PackageManager.PERMISSION_DENIED
        ) {

            Snackbar.make(
                requireView(),
                "location permission is required ",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("settings") {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()

        } else {
           deviceChecking()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
