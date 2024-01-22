package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private var rvPlaces: RecyclerView? = null
    private var tvNoRecords: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvPlaces = findViewById(R.id.rv_happy_places_list)
        tvNoRecords = findViewById(R.id.tv_no_records_available)

        val fabAddPlace: FloatingActionButton = findViewById(R.id.fabHappyPlaces)
        fabAddPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_REQUEST_CODE)
        }

        getHappyPlaceListFromLocalDB()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_PLACE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getHappyPlaceListFromLocalDB()
            } else {
                Log.i("Activity", "Cancelled or Back pressed")
            }
        }
    }

    private fun getHappyPlaceListFromLocalDB() {
        val dbHandler = DatabaseHandler(this@MainActivity)
        val happyPlacesList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(happyPlacesList.size > 0) {
            rvPlaces?.visibility = View.VISIBLE
            tvNoRecords?.visibility = View.GONE
            setupHappyPlacesRV(happyPlacesList)
        } else {
            rvPlaces?.visibility = View.GONE
            tvNoRecords?.visibility = View.VISIBLE
        }
    }

    private fun setupHappyPlacesRV(places: ArrayList<HappyPlaceModel>) {
        rvPlaces?.layoutManager = LinearLayoutManager(this@MainActivity)
        rvPlaces?.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this@MainActivity, places)
        rvPlaces?.adapter = placesAdapter
        placesAdapter.setOnClickListener(object: HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        // edit place handler
        val editSwipeHandler = object: SwipeToEditCallback(this@MainActivity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvPlaces?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvPlaces)

        // delete place handler
        val deleteSwipeHandler = object: SwipeToDeleteCallback(this@MainActivity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvPlaces?.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getHappyPlaceListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvPlaces)
    }

    companion object {
        private const val ADD_PLACE_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}