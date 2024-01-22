package com.example.trelloclone.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityProfileBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.example.trelloclone.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.example.trelloclone.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.example.trelloclone.utils.Constants.getFileExtension
import com.example.trelloclone.utils.Constants.showImageChooser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class ProfileActivity : BaseActivity() {
    private var binding: ActivityProfileBinding? = null
    private var mSelectedImageFileURI: Uri? = null
    private var mProfileImageDownloadableURL: String = ""
    private lateinit var mUserDetails: User

    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
        FirestoreClass().loadUserData(this@ProfileActivity)

        binding?.ivProfileUserImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@ProfileActivity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this@ProfileActivity,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnUpdate?.setOnClickListener {
            if (mSelectedImageFileURI != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser(this)
            } else {
                Toast.makeText(this, "Oops, you just denied the permission for storage. Allow it from the settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
    // uncheck this if u want to have no animation for the email field. also clear the disabled flag set on the field
    // and call the method in onCreate
    @SuppressLint("ClickableViewAccessibility")
    private fun setupEmailNonTouchable() {
        val emailField: AppCompatEditText? = binding?.etEmail
        emailField?.setOnTouchListener { view, event -> true }
    }
     */

    private fun setupActionBar() {
        val profileToolbar: Toolbar? = binding?.toolbarMyProfileActivity
        setSupportActionBar(profileToolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        profileToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user

        val profileUserImage: CircleImageView? = binding?.ivProfileUserImage
        val navUserName: TextView? = binding?.etName
        val navUserEmail: TextView? = binding?.etEmail
        val navUserMobile: TextView? = binding?.etMobile

        if (profileUserImage != null) {
            Glide
                .with(this@ProfileActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(profileUserImage)
        }

        if (navUserName != null) {
            navUserName.text = user.name
        }

        if (navUserEmail != null) {
            navUserEmail.text = user.email
        }

        if (navUserMobile != null && user.mobileNumber != 0L) {
//            val formattedNumber = resources.getString(R.string.formatted_full_mobile_number, Constants.MOBILE_NUMBER_RO_PREFIX, user.mobileNumber)
//            navUserMobile.text = formattedNumber
            navUserMobile.text = user.mobileNumber.toString()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data != null && data.data != null) {
            mSelectedImageFileURI = data.data

            try {
                val profileUserImage: CircleImageView? = binding?.ivProfileUserImage
                if (profileUserImage != null) {
                    Glide
                        .with(this@ProfileActivity)
                        .load(mSelectedImageFileURI)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(profileUserImage)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileURI != null) {
            val storageReference: StorageReference =
                FirebaseStorage.getInstance().reference.child("USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(this, mSelectedImageFileURI))

            storageReference
                .putFile(mSelectedImageFileURI!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i("Firebase Image URL", taskSnapshot.metadata?.reference?.downloadUrl.toString())
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        Log.i("Downloadable URI", uri.toString())
                        mProfileImageDownloadableURL = uri.toString()

                        updateUserProfileData()
                    }
                }
                .addOnFailureListener {exception ->
                    Toast.makeText(this@ProfileActivity, exception.message, Toast.LENGTH_SHORT).show()
                    hideProgressDialog()
                }
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        var changesFlag = false

        if(mProfileImageDownloadableURL.isNotEmpty() && mProfileImageDownloadableURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageDownloadableURL
            changesFlag = true
        }

        if (binding?.etName?.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            changesFlag = true
        }

        if (binding?.etMobile?.text.toString() != mUserDetails.name) {
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
            changesFlag = true
        }

        if (changesFlag) {
            FirestoreClass().updateUserProfileData(this@ProfileActivity, userHashMap)
            hideProgressDialog()
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}