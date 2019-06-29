package com.jkjk.quicknote.taskeditscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.jkjk.quicknote.R
import kotlinx.android.synthetic.main.activity_location.*
import java.io.IOException


/**
 *Created by chrisyeung on 21/11/2018.
 */

@SuppressLint("MissingPermission")
class LocationAct : AppCompatActivity(),
//        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnPoiClickListener,
        LocationAdapter.OnLocationSelectListener{

    private var mMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null

    private var search: MenuItem? = null

    private val adapter: LocationAdapter by lazy {
        LocationAdapter(this, this)
    }

    // life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        selectedLatLng = intent?.getParcelableExtra(EXTRA_LOCATION_LAT_LNG)

        obtainLocationPermission(false)

        setupBody()
    }

    override fun onPause() {
        mMap?.setOnPoiClickListener(null)
        mMap?.setOnMapClickListener(null)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMap?.setOnMapClickListener(this)
        mMap?.setOnPoiClickListener(this)
    }
    // life cycle

    // UI Present
    fun setupBody() {

        setSupportActionBar(menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.location)


        ((supportFragmentManager.findFragmentById(R.id.map)) as SupportMapFragment).getMapAsync(this)

        rvLocation?.layoutManager = LinearLayoutManager(this)
        rvLocation?.adapter = adapter
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.location_menu, menu)

        search = menu?.findItem(R.id.search)

        val searchView = menu?.findItem(R.id.search)?.actionView as? SearchView
        searchView?.queryHint = resources.getString(R.string.search)
        searchView?.isSubmitButtonEnabled = false

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val latLngList = getLocationsOfName(query ?: return false)

                if (latLngList.size > 1) {
                    onMapClick(latLngList.first())
                    val boundBuilder = LatLngBounds.builder()
                    latLngList.forEach {
                        boundBuilder.include(it)
                        mMap!!.addMarker(MarkerOptions().draggable(false).position(it).anchor(0.5f, 1f))
                    }
                    search?.collapseActionView()
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 20), 350, null)
                } else if (latLngList.size == 1) {
                    onMapClick(latLngList.first())
                    search?.collapseActionView()
                } else {
                    AlertDialog.Builder(this@LocationAct).setMessage(R.string.not_found)
                            .setPositiveButton(R.string.ok, null)
                            .show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        return true
    }

    override fun onMapReady(map: GoogleMap?) {
        if (mMap == null && map != null) {
            mMap = map
            mMap!!.uiSettings.isMapToolbarEnabled = false
            mMap!!.isMyLocationEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            mMap!!.setOnMapClickListener(this)
            mMap!!.setOnPoiClickListener(this)

            if (selectedLatLng != null) {
                mMap!!.addMarker(MarkerOptions().draggable(false).position(selectedLatLng!!).anchor(0.5f, 1f))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14f))
            }

        }
    }
    // UI Present

//    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.imgActionBarRight -> {
//                v.visibility = View.GONE
//                cardViewSearch?.visibility = View.VISIBLE
//                txtSearch?.requestFocus()
//            }
//            R.id.btnBack -> {
//                onBackPressed()
//            }
//        }
//    }
//
    override fun onBackPressed() {
        if (search?.isActionViewExpanded == true) {
           search?.collapseActionView()
        } else super.onBackPressed()
    }

    // Map related
    override fun onMapClick(latLng: LatLng?) {
        if (latLng != null) {
            val selectedLocationNames = getNamesOfLocation(latLng)
            presentNamesOfLatLng(selectedLocationNames, latLng)
        }
    }

    override fun onPoiClick(poi: PointOfInterest?) {
        if (poi?.latLng != null) {
            val selectedLocationNames = arrayListOf<String>()
            if (poi.name != null) {
                selectedLocationNames.add(poi.name)
            }
            selectedLocationNames.addAll(getNamesOfLocation(poi.latLng))
            presentNamesOfLatLng(selectedLocationNames, poi.latLng)
        }
    }

    override fun onLocationSelect(location: String?) {
        if (location == null) {
            if (obtainLocationPermission(true)) {
                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                mFusedLocationClient.requestLocationUpdates(LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(60000L), object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        val current = locationResult?.lastLocation ?: return
                        val currentLatLng = LatLng(current.latitude, current.longitude)
                        val selectedLocationNames = getNamesOfLocation(currentLatLng)
                        setResult(Activity.RESULT_OK, Intent()
                                .putExtra(EXTRA_LOCATION_NAME, selectedLocationNames.firstOrNull())
                                .putExtra(EXTRA_LOCATION_LAT_LNG, currentLatLng))
                        mFusedLocationClient.removeLocationUpdates(this)
                        finish()
                    }
                }, null)
            }
        } else {
            setResult(Activity.RESULT_OK, Intent()
                    .putExtra(EXTRA_LOCATION_NAME, location)
                    .putExtra(EXTRA_LOCATION_LAT_LNG, selectedLatLng))
            finish()
        }
    }

    private fun presentNamesOfLatLng(listOfName: List<String>, latLng: LatLng?) {
        if (mMap !=  null) {
            mMap!!.clear()
            if (latLng != null) {
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap!!.cameraPosition.zoom), 350, null)
                mMap!!.addMarker(
                        MarkerOptions().draggable(false).position(latLng).anchor(0.5f, 1f)
                )
            }
        }

        selectedLatLng = latLng
        adapter.locationList = listOfName
        adapter.notifyDataSetChanged()
    }

    private fun getNamesOfLocation(latLng: LatLng): List<String> {
        val resultNames = arrayListOf<String>()

        val geocoder = Geocoder(this,
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) resources.configuration.locale
                else resources.configuration.locales.get(0))

        try {
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 10)

            addressList.forEach { address ->
                if (address.maxAddressLineIndex > -1) {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until address.maxAddressLineIndex + 1) {
                        if (stringBuilder.isNotEmpty()) {
                            stringBuilder.append(", ")
                        }
                        stringBuilder.append(address.getAddressLine(i))
                    }
                    resultNames.add(stringBuilder.toString())
                }
            }
        } catch (e: IOException) {
        }
        return resultNames.distinct()
    }

    private fun getLocationsOfName(name: String): List<LatLng> {
        val geocoder = Geocoder(this,
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) resources.configuration.locale
                else resources.configuration.locales.get(0))

        val addressList = try {
            geocoder.getFromLocationName(name, 10)
        } catch (e: IOException) {
            listOf<Address>()
        }
        val resultLatLng = addressList?.map { LatLng(it.latitude, it.longitude) } ?: listOf()

        return resultLatLng.distinct()
    }

    /**
     * Permission related
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == LOCATION_REQUEST_CODE) {
            val targetIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (targetIndex >= 0 && grantResults[targetIndex] == PackageManager.PERMISSION_GRANTED) {
                mMap?.isMyLocationEnabled = true
            } else {
                showLocationPermissionDialog()
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap?.isMyLocationEnabled = true
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun obtainLocationPermission(showPrompt: Boolean): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Permission has been reject
                    if (showPrompt) {
                        showLocationPermissionDialog()
                    }
                } else {
                    // Permission has not been asked / Don't ask again has been checked
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_REQUEST_CODE
                    )
                }
                false
            } else true
        } else true // Lollipop
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this).setPositiveButton(R.string.ok) { _, _ ->
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_REQUEST_CODE
                )
            } else {
                val permissionPageIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                startActivityForResult(permissionPageIntent,
                        LOCATION_REQUEST_CODE
                )
            }
        }.setCancelable(true)
                .setNegativeButton(R.string.cancel, null).setMessage(R.string.location_permission_msg).show()

    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 222
        const val EXTRA_LOCATION_NAME = "location name"
        const val EXTRA_LOCATION_LAT_LNG = "location lat lng"
    }
}