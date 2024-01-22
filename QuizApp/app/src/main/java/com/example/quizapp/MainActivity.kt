package com.example.quizapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText: EditText = findViewById(R.id.inputText)
        val btnStart: Button = findViewById(R.id.btnStart)
        btnStart.setOnClickListener {
            if (inputText.text.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Hi there, ${inputText.text}", Toast.LENGTH_SHORT).show()

                // create an intent to move from one screen to another
                val intent = Intent(this, QuizQuestionsActivity::class.java)

                intent.putExtra(Constants.USER_NAME, inputText.text.toString())

                //starts the intent
                startActivity(intent)

                // closes man activity so you can't go back to main screen
                finish()
            }
        }
    }
}