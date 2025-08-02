package com.example.onetapalarm // <-- VERY IMPORTANT

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    var isEnabled: Boolean
)