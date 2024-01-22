package com.example.weatherapp.models

import java.io.Serializable

data class WeatherObject(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
): Serializable
