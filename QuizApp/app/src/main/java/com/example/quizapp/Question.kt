package com.example.quizapp

data class Question(
    val id: Int,
    val questionText: String,
    val imagePos: Int,

    val optionOne: String,
    val optionTwo: String,
    val optionThree: String,
    val optionFour: String,

    val correctAnswer: Int
)
