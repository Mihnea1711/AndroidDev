package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.IWeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class MainActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    private var tvMain: TextView? = null
    private var tvMainDescription: TextView? = null
    private var tvTemp: TextView? = null
    private var tvHumidity: TextView? = null
    private var tvMinTemp: TextView? = null
    private var tvMaxTemp: TextView? = null
    private var tvWindSpeed: TextView? = null
    private var tvWindUnits: TextView? = null
    private var tvName: TextView? = null
    private var tvCountry: TextView? = null
    private var tvSunriseTime: TextView? = null
    private var tvSunsetTime: TextView? = null
    private var ivIconMain: ImageView? = null

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMain = findViewById(R.id.tv_main)
        tvMainDescription = findViewById(R.id.tv_main_description)
        tvTemp = findViewById(R.id.tv_temp)
        tvHumidity = findViewById(R.id.tv_humidity)
        tvMinTemp = findViewById(R.id.tv_min)
        tvMaxTemp = findViewById(R.id.tv_max)
        tvWindSpeed = findViewById(R.id.tv_speed)
        tvWindUnits = findViewById(R.id.tv_speed_unit)
        tvName = findViewById(R.id.tv_name)
        tvCountry = findViewById(R.id.tv_country)
        tvSunriseTime = findViewById(R.id.tv_sunrise_time)
        tvSunsetTime = findViewById(R.id.tv_sunset_time)
        ivIconMain = findViewById(R.id.iv_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        // select private so the info stored is visible by only this app
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        setupUI()

        if (!isLocationEnabled()) {
            Toast.makeText(this@MainActivity, "Your location provider is off. Please turn it on!", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if(report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }
                        if(report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(this@MainActivity, "You have denied location. Please turn it on!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken?) {
                        showRationaleDialogForPermissions()
                    }
                }).onSameThread().check()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, android.location.LocationRequest.PASSIVE_INTERVAL)
            .setWaitForAccurateLocation(false)
            .build()

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            val latitude: Double? = mLastLocation?.latitude
            val longitude: Double? = mLastLocation?.longitude

            getLocationWeatherDetails(latitude!!, longitude!!)
        }
    }

    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(this@MainActivity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val weatherService: IWeatherService = retrofit.create(IWeatherService::class.java)

            val listCall: Call<WeatherResponse> = weatherService.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.API_KEY
            )

            showCustomProgressDialog()
            listCall.enqueue(object: Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        hideCustomProgressDialog()
                        val weatherList: WeatherResponse? = response.body()

                        Log.i("WEATHER DATA", weatherList.toString())

                        // store weather response in shared pref
                        val weatherResponseJSONString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJSONString)
                        editor.apply()

                        setupUI()
                    } else {
                        when (response.code()) {
                            400 -> Log.e("ERROR 400", "Bad Connection")
                            404 -> Log.e("ERROR 404", "Not Found")
                            else -> Log.e("ERROR", "Generic error")
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Custom Error", t.message.toString())
                    hideCustomProgressDialog()
                }

            })
        } else {
            Toast.makeText(this@MainActivity, "You don't have a good connection to the internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this@MainActivity)

        mProgressDialog!!.setContentView(R.layout.custom_progress_dialog)

        mProgressDialog!!.show()
    }

    private fun hideCustomProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {
            val weatherResponse: WeatherResponse = Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            for (item in weatherResponse.weather.indices) {
                Log.i("Weather name", weatherResponse.toString())
                Log.i("weather", weatherResponse.toString())

                tvMain?.text = weatherResponse.weather[item].main
                tvMainDescription?.text = weatherResponse.weather[item].description

                tvTemp?.text =
                    "${weatherResponse.main.temp}${getLocaleUnit(application.resources.configuration.toString())}"
                tvHumidity?.text = weatherResponse.main.humidity.toString() + " per cent"

                tvMinTemp?.text = weatherResponse.main.temp_min.toString() + " min"
                tvMaxTemp?.text = weatherResponse.main.temp_max.toString() + " max"

                tvWindSpeed?.text = weatherResponse.wind.speed.toString()

                tvName?.text = weatherResponse.name
                tvCountry?.text = weatherResponse.sys.country

                tvSunriseTime?.text = unixTime(weatherResponse.sys.sunrise.toLong())
                tvSunsetTime?.text = unixTime(weatherResponse.sys.sunset.toLong())

                // Here we update the main icon
                when (weatherResponse.weather[item].icon) {
                    "01d" -> ivIconMain?.setImageResource(R.drawable.sunny)
                    "02d" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "03d" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "04d" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "04n" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "10d" -> ivIconMain?.setImageResource(R.drawable.rain)
                    "11d" -> ivIconMain?.setImageResource(R.drawable.storm)
                    "13d" -> ivIconMain?.setImageResource(R.drawable.snowflake)
                    "01n" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "02n" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "03n" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "10n" -> ivIconMain?.setImageResource(R.drawable.cloud)
                    "11n" -> ivIconMain?.setImageResource(R.drawable.rain)
                    "13n" -> ivIconMain?.setImageResource(R.drawable.snowflake)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_refresh -> {
                requestLocationData()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
//
    }

    private fun getLocaleUnit(value: String): String {
        var unit = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            unit = "°F"
        }
        return unit
    }

    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}