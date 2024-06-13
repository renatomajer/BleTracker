package com.renatomajer.bletracker.data.remote

import com.renatomajer.bletracker.data.remote.dto.DeviceIdResponse
import com.renatomajer.bletracker.data.remote.dto.LoginRequest
import com.renatomajer.bletracker.data.remote.dto.StopStealing
import com.renatomajer.bletracker.data.remote.dto.Telemetry
import com.renatomajer.bletracker.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @POST("api/auth/login")
    suspend fun getTokenForUsernameAndPassword(
        @Body loginRequest: LoginRequest
    ): TokenResponse


    /**
     * {
     *     "location": [
     *         {
     *             "ts": 1718141056576,
     *             "value": "45.80147649172565, 15.971030858769527"
     *         }
     *     ],
     *     "status": [
     *         {
     *             "ts": 1718141056576,
     *             "value": "1"
     *         }
     *     ]
     * }
     */
    @GET("api/plugins/telemetry/DEVICE/{deviceId}/values/timeseries?keys=location,status")
    suspend fun getTelemetry(
        @Header("Authorization") token: String,
        @Path("deviceId") deviceId: String
    ): Telemetry

    @POST("api/plugins/rpc/oneway/{deviceId}")
    suspend fun stopStealing(
        @Header("Authorization") token: String,
        @Body body: StopStealing,
        @Path("deviceId") deviceId: String
    )

    @GET("api/customer/{customerId}/devices?pageSize=5&page=0")
    suspend fun getDeviceId(
        @Header("Authorization") token: String,
        @Path("customerId") customerId: String
    ): DeviceIdResponse
}