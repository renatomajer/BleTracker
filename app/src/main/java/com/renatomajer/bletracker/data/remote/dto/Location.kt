package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class Location(
    val ts: Long,
    val value: String
): Serializable
