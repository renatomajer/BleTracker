package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class DeviceIdResponse(
    val data: List<PlatformInfo>
) : Serializable


data class PlatformInfo(
    val id: IdInfo
) : Serializable

data class IdInfo(
    val entityType: String,
    val id: String
) : Serializable