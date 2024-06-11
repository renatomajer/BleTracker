package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class TokenResponse(
    val token: String,
    val refreshToken: String
):Serializable
