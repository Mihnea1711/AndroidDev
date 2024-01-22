package com.example.weatherapp.models

import java.io.Serializable

data class SysObject (
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Int,
    val sunset: Int
): Serializable