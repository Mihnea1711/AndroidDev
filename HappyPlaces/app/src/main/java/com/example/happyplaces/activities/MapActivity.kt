package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mHappyPlaceModel: HappyPlaceModel? = null
    private var tbMap: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        tbMap = findViewById(R.id.toolbar_map)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java) as HappyPlaceModel
        }

        if (mHappyPlaceModel != null) {
            setSupportActionBar(tbMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceModel!!.title

            tbMap?.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this@MapActivity)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        /**
         * Add a marker on the location using the latitude and longitude and move the camera to it.
         */
        val position =
            mHappyPlaceModel?.latitude?.let { lat ->
            mHappyPlaceModel?.longitude?.let { long ->
                LatLng(lat, long)
        }}

        position?.let {
            MarkerOptions()
                .position(it)
                .title(mHappyPlaceModel?.location)
        }?.let { map.addMarker(it) }

        val newLatLngZoom = position?.let { CameraUpdateFactory.newLatLngZoom(it, 10f) }
        if (newLatLngZoom != null) {
            map.animateCamera(newLatLngZoom)
        }
    }
}