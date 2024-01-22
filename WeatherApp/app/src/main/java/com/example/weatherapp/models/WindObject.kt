package com.example.weatherapp.models

import java.io.Serializable

data class WindObject (
    val speed: Double,
    val deg: Int,
    val gust: Double
): Serializable