package com.example.trelloclone.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloclone.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private lateinit var mProgressDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    protected fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this@BaseActivity)
        mProgressDialog.setContentView(R.layout.dialog_progress)

        val tvProgress: TextView = mProgressDialog.findViewById(R.id.tv_progress_text)
        tvProgress.text = text

        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        this.mProgressDialog.hide()
    }

    protected fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    protected fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            // if pressed twice exit app
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        // if pressed once, show toast to user
        Toast.makeText(this, resources.getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()

        // after 2 sec reset everything so the user can press back btn once without exiting the app
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snack_bar_error_color))

        snackBar.show()
    }
}