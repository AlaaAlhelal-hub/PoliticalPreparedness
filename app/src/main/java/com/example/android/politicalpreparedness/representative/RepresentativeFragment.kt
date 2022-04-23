package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListener
import com.example.android.politicalpreparedness.representative.adapter.setNewValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.function.Consumer

class DetailFragment : Fragment(), LocationListener {

    companion object {
        private val REQUEST_LOCATION_PERMISSION = 100
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }



    private val viewModel: RepresentativeViewModel by viewModels()
    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var adapter: RepresentativeListAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)

        adapter = RepresentativeListAdapter(RepresentativeListener {
        })

        binding.representativesRecyclerView.adapter = adapter



        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.addressInputMutableLiveData.value = Address(
                line1 = binding.addressLine1.text.toString(),
                line2= binding.addressLine2.text.toString(),
                city= binding.city.text.toString(),
                zip = binding.zip.text.toString(),
                state = binding.state.selectedItem.toString()
            )
            viewModel.getAddressLocationFromFields()
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            checkLocationPermissions()
        }

        observeViewModel()

        binding.viewModelRep = viewModel


        return binding.root
    }

    private fun observeViewModel(){
        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.responseError.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })

        viewModel.addressInputMutableLiveData.observe(viewLifecycleOwner, Observer {
            binding.addressLine1.setText(it.line1)
            binding.addressLine2.setText(it.line2)
            binding.city.setText(it.city)
            binding.zip.setText(it.zip)
            binding.state.setNewValue(it.state)
        })

        viewModel.loadingData.observe(viewLifecycleOwner, Observer { loading ->
            if (loading)
                binding.progressbarLoadRep.visibility = View.VISIBLE
            else
                binding.progressbarLoadRep.visibility = View.GONE

        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
             val snackbar = Snackbar.make(binding.root, "You need to grant location permission in order to select the location", Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings") {
                    if (isAdded) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                }
            snackbar.show()
        }
    }


    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            getLocation()
            true
        } else {
            Snackbar.make(requireView(),
                "Please allow location permission", Snackbar.LENGTH_LONG ).show()
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION)

            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient.getLastLocation()
            .addOnCompleteListener(OnCompleteListener<Location?> { task ->
                val location: Location? = task.getResult()
                if (location != null) {
                    val address = geoCodeLocation(location)

                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Location services must be enabled to use the app")
                        .setPositiveButton(R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            })

    }


    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun moveToCurrentLocation(provider: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isPermissionGranted()) {
                locationManager.getCurrentLocation(provider, null, requireContext().mainExecutor, Consumer {
                   Log.i("UserLocation",it.toString())
                    geoCodeLocation(it)
                })
            }
        } else {
            locationManager.requestSingleUpdate(provider, this, Looper.getMainLooper())
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                viewModel.getGeoLocation(Address(line1 = address.thoroughfare, line2 = address.subThoroughfare, line3= null, locationName = null, city = address.locality, state= address.adminArea, zip = address.postalCode))

                Log.i("address : ",address.toString())
                Address(line1 = address.thoroughfare, line2 = address.subThoroughfare, line3= null, locationName = null, city = address.locality, state= address.adminArea, zip = address.postalCode)

            }
            .first()
    }

    override fun onLocationChanged(location: Location) {
        viewModel.getGeoLocation(geoCodeLocation(location))
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }


}