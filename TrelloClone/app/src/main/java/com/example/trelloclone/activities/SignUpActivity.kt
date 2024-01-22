package com.example.trelloclone.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivitySignUpBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setupActionBar()

        binding?.btnSignUp?.setOnClickListener {
            // hide the keyboard after btn pressed
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(binding?.llSignupFields?.windowToken, 0)

            registerUser()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24dp)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun registerUser() {
        val name: String = binding?.etName?.text.toString().trim()
        val email: String = binding?.etEmail?.text.toString().trim()
        val password: String = binding?.etPassword?.text.toString().trim()

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            // authenticate user if everything is ok
            FirebaseAuth
                .getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        val user = User(firebaseUser.uid, name, registeredEmail)

                        FirestoreClass().registerUser(this, user)
                    } else {
                        Log.w("Sign Up", "sign Up failure", task.exception)
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name...")
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email...")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password...")
                false
            }

            else -> true
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "you have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()
        hideProgressDialog()
//        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}