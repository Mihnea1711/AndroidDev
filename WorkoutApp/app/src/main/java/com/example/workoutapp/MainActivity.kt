package com.example.workoutapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.FrameLayout
import android.widget.Toast
import com.example.workoutapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mainBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding?.root)

        mainBinding?.flStart?.setOnClickListener {
            // move to exercise activity
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }

        mainBinding?.flBMI?.setOnClickListener {
            // move to bmi calc activity
            val intent = Intent(this, BMIActivity::class.java)
            startActivity(intent)
        }

        mainBinding?.flHistory?.setOnClickListener {
            // move to bmi calc activity
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

//    important to set binding to null
    override fun onDestroy() {
        super.onDestroy()
        mainBinding = null
    }
}