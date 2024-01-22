package com.example.trelloclone.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import com.example.trelloclone.databinding.ActivitySplashBinding
import com.example.trelloclone.firebase.FirestoreClass

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // deprecated method
        /*
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
         */

        // new method
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             window.insetsController?.hide(WindowInsets.Type.statusBars())
         } else {
             window.setFlags(
                 WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN
             )
         }

        // set font for the text view text
        val typeFace: Typeface  = Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding?.tvAppName?.typeface = typeFace

        // delay and show this activity for .5 sec then go to intro activity
        // either use this or  use coroutines with delay(2500)
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUserID = FirestoreClass().getCurrentUserID()
            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2500)

        /*
        // this way is ok too, but don t forget the gradle implementations!
        lifecycleScope.launch {
            delay(2500)
            startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            finish()
        }
         */
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}