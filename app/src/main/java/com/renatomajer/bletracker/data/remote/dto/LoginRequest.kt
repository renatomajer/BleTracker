package com.renatomajer.bletracker.data.remote.dto

import java.io.Serializable

data class LoginRequest(
    val username: String,
    val password: String
): Serializable
