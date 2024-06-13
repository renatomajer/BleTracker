package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class Telemetry(
    val location: List<Location>,
    val status: List<Status>
) : Serializable
