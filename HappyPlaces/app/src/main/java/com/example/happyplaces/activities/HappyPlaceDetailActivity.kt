package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var tbPlaceDetail: Toolbar? = null
    private var ivImageDetail: ImageView? = null
    private var tvDescriptionDetail: TextView? = null
    private var tvLocationDetail: TextView? = null
    private var btnViewOnMap: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)

        tbPlaceDetail = findViewById(R.id.toolbar_happy_place_detail)
        ivImageDetail = findViewById(R.id.iv_place_image)
        tvDescriptionDetail = findViewById(R.id.tv_description)
        tvLocationDetail = findViewById(R.id.tv_location)
        btnViewOnMap = findViewById(R.id.btn_view_on_map)

        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            // get the Serializable data model class with the details in it
            happyPlaceDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java) as HappyPlaceModel
        }

        if (happyPlaceDetailModel != null) {
            setSupportActionBar(tbPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            tbPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }

            ivImageDetail?.setImageURI(Uri.parse(happyPlaceDetailModel.imagePath))
            tvDescriptionDetail?.text = happyPlaceDetailModel.description
            tvLocationDetail?.text = happyPlaceDetailModel.location

            btnViewOnMap?.setOnClickListener {
                val intent = Intent(this@HappyPlaceDetailActivity, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
                startActivity(intent)
            }
        }
    }
}