package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class StopStealing(
    val method: String,
    val params: Params
): Serializable
