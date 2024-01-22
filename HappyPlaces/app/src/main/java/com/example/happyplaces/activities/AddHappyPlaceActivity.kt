package com.example.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private var calendar = Calendar.getInstance()
    private lateinit var dateSetListener: OnDateSetListener

    private var saveImageToInternalStoragePath: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var etDate: AppCompatEditText? = null
    private var tvAddImage: TextView? = null
    private var ivImage: ImageView? = null
    private var btnSave: Button? = null
    private var etTitle: EditText? = null
    private var etDescription: EditText? = null
    private var etLocation: EditText? = null
    private var tvCurrentLocation: TextView? = null

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private val startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    etLocation?.setText(place.address)
                    mLatitude = place.latLng?.latitude ?: 0.0
                    mLongitude = place.latLng?.longitude ?: 0.0
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i("Places", "User canceled autocomplete")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        ivImage = findViewById(R.id.iv_place_image)
        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)
        etDate = findViewById(R.id.et_date)
        tvAddImage = findViewById(R.id.tv_add_image)
        btnSave = findViewById(R.id.btn_save)
        tvCurrentLocation = findViewById(R.id.tv_select_current_location)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@AddHappyPlaceActivity)

        val tbAddPlace: Toolbar = findViewById(R.id.tbAddPlace)
        setSupportActionBar(tbAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tbAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        if(!Places.isInitialized()) {
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.maps_api_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java) as HappyPlaceModel
        }

        dateSetListener = OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }
        updateDateInView()

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"

            // fill in fields
            etTitle?.setText(mHappyPlaceDetails!!.title)
            etDescription?.setText(mHappyPlaceDetails!!.description)
            etDate?.setText(mHappyPlaceDetails!!.date)
            etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStoragePath = Uri.parse(mHappyPlaceDetails!!.imagePath)
            ivImage?.setImageURI(saveImageToInternalStoragePath)
            btnSave?.text = "UPDATE"
        }

        etDate?.setOnClickListener(this)
        tvAddImage?.setOnClickListener(this)
        btnSave?.setOnClickListener(this)
        etLocation?.setOnClickListener(this)
        tvCurrentLocation?.setOnClickListener(this)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.Builder(0)
            .setIntervalMillis(10000)
            .setMaxUpdates(1)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location? = locationResult.lastLocation
            mLatitude = lastLocation!!.latitude
            Log.i("Lat", mLatitude.toString())
            mLongitude = lastLocation.longitude
            Log.i("Lon", mLongitude.toString())

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.IAddressListener {
                override fun onAddressFound(address: String?) {
                    etLocation?.setText(address)
                }
                override fun onError() {
                    Log.i("Address LatLon", "Smth went wrong")
                }
            })

            addressTask.getAddress()
        }
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH
                )).show()
            }

            R.id.et_location -> {
                try {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    // Start the autocomplete intent with a unique request code.
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddHappyPlaceActivity)
//                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                    startAutocomplete.launch(intent)


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            R.id.tv_select_current_location -> {
                if(!isLocationEnabled()) {
                    Toast.makeText(this@AddHappyPlaceActivity, "Location off. Please turn on!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object: MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                requestNewLocationData()
                            }
                        }
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                            showRationaleDialogForPermissions()
                        }
                    }).onSameThread().check()
                }
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")

                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")

                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> {
                            choosePhotoFromGallery()
                        }
                        1 -> {
                            takePhotoFromCamera()
                        }
                    }
                }

                pictureDialog.show()
            }

            R.id.btn_save -> {
                // store data model to db
                when {
                    etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a title", Toast.LENGTH_SHORT).show()
                    }
                    etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }
                    etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStoragePath == null -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please select an image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val happyPlaceModel: HappyPlaceModel = HappyPlaceModel(
                            if(mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.uid,
                            etTitle?.text.toString(),
                            saveImageToInternalStoragePath.toString(),
                            etDescription?.text.toString(),
                            etDate?.text.toString(),
                            etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler = DatabaseHandler(this)
                        if (mHappyPlaceDetails == null) {
                            val addHappyPlaceResult = dbHandler.addHappyPlace(happyPlaceModel)
                            if (addHappyPlaceResult > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(this@AddHappyPlaceActivity, "failed to insert", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val updateHappyPlaceResult = dbHandler.updateHappyPlace(happyPlaceModel)
                            if (updateHappyPlaceResult > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(this@AddHappyPlaceActivity, "failed to update", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalStoragePath = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image : ", "Path :: $saveImageToInternalStoragePath")

                        ivImage?.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (requestCode == CAMERA_REQUEST_CODE) {
                if (data != null) {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap // Bitmap from camera

                    saveImageToInternalStoragePath = saveImageToInternalStorage(thumbnail)
                    Log.e("Saved Image : ", "Path :: $saveImageToInternalStoragePath")

                    ivImage?.setImageBitmap(thumbnail)
                }
            }
//            else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
//                if (data != null) {
//                    val place: Place = Autocomplete.getPlaceFromIntent(data)
//                    etLocation?.setText(place.address)
//                    mLatitude = place.latLng?.latitude ?: 0.0
//                    mLongitude = place.latLng?.longitude ?: 0.0
//                }
//            }
        }
    }

    private fun choosePhotoFromGallery() {
        // the code for gallery picker works but for some reason dexter code doesn t
        /*
        Dexter.withContext(this@AddHappyPlaceActivity).withPermissions(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
        */


        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    private fun takePhotoFromCamera() {
        // the code for camera works but for some reason dexter code doesn t
        /*
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // Here after all the permission are granted launch the CAMERA to capture an image.
                    if (report!!.areAllPermissionsGranted()) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread()
            .check()
         */

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    // this function should be shown to the user if he doesn t allow permissions but we don t ask for those bcuz dexter not working :/
    private fun showRationaleDialogForPermissions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permission required for this feature. It can be enabled under the App Settings")
            .setPositiveButton("Go to Settings") { _, _ ->
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
            }
            .show()
    }

    private fun updateDateInView() {
        val myFormat = "dd MM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        etDate?.setText(sdf.format(calendar.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file = wrapper.getDir(IMAGE_DIR, Context.MODE_PRIVATE)

        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        private const val IMAGE_DIR = "HappyPlacesImages"
    }
}