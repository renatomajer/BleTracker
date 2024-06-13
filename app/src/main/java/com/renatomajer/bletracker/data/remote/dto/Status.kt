package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class Status(
    val ts: Long,
    val value: String
) : Serializable
