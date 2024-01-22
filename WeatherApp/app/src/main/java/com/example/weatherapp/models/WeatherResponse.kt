package com.example.weatherapp.models

import java.io.Serializable

data class WeatherResponse (
    val coordinates: CoordObject,
    val weather: List<WeatherObject>,
    val base: String,
    val main: MainObject,
    val visibility: Int,
    val wind: WindObject,
    val rain: RainObject,
    val clouds: CloudsObject,
    val dt: Int,
    val sys: SysObject,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
): Serializable
