package com.renatomajer.bletracker.data.remote

import com.renatomajer.bletracker.data.remote.dto.LoginRequest
import com.renatomajer.bletracker.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {
    @POST("api/auth/login")
    suspend fun getTokenForUsernameAndPassword(
        @Body loginRequest: LoginRequest
    ): TokenResponse
}