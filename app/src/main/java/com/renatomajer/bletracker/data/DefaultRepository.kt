package com.renatomajer.bletracker.data

import com.renatomajer.bletracker.data.local.CredentialsDataStore
import com.renatomajer.bletracker.data.remote.ApiService
import com.renatomajer.bletracker.data.remote.Result
import com.renatomajer.bletracker.data.remote.dto.LoginRequest
import com.renatomajer.bletracker.data.remote.dto.Params
import com.renatomajer.bletracker.data.remote.dto.StopStealing
import com.renatomajer.bletracker.data.remote.dto.Telemetry
import com.renatomajer.bletracker.data.remote.dto.TokenResponse
import com.renatomajer.bletracker.data.remote.safeResponse
import com.renatomajer.bletracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRepository @Inject constructor(
    private val api: ApiService,
    private val credentialsDataStore: CredentialsDataStore
) {

    val username = credentialsDataStore.username

    val password = credentialsDataStore.password

    val token = credentialsDataStore.token

    val refreshToken = credentialsDataStore.refreshToken

    val deviceId = credentialsDataStore.deviceId

    suspend fun getToken(username: String, password: String): Flow<Resource<TokenResponse>> = flow {
        emit(Resource.Loading())

        val loginRequest = LoginRequest(username = username, password = password)

        val result = safeResponse {
            api.getTokenForUsernameAndPassword(loginRequest)
        }

        when (result) {
            is Result.Success -> {
                emit(Resource.Success(data = result.data))
            }

            is Result.Error -> {
                emit(
                    Resource.Error(
                        errorMessage = result.error.message ?: "",
                        error = result.error
                    )
                )
            }
        }
    }

    suspend fun storeUsername(username: String) {
        credentialsDataStore.setUsername(username)
    }

    suspend fun storePassword(password: String) {
        credentialsDataStore.setPassword(password)
    }

    suspend fun storeToken(token: String) {
        credentialsDataStore.setToken(token)
    }

    suspend fun storeRefreshToken(refreshToken: String) {
        credentialsDataStore.setRefreshToken(refreshToken)
    }

    suspend fun getTelemetry(): Flow<Resource<Telemetry>> = flow {
        emit(Resource.Loading())

        val deviceId = deviceId.first()
        val storedToken = token.first()

        if (deviceId != null && storedToken != null) {
            val result = safeResponse {
                api.getTelemetry(token = "Bearer $storedToken", deviceId = deviceId)
            }

            when (result) {
                is Result.Success -> {
                    emit(Resource.Success(data = result.data))
                }

                is Result.Error -> {
                    emit(
                        Resource.Error(
                            errorMessage = result.error.message ?: "",
                            error = result.error
                        )
                    )
                }
            }
        }
    }

    suspend fun stopStealing() {
        val storedToken = token.first()
        val deviceId = deviceId.first()

        if (storedToken != null && deviceId != null) {
            api.stopStealing(
                token = "Bearer $storedToken",
                body = StopStealing(
                    method = "stopGPS",
                    params = Params(false)
                ),
                deviceId = deviceId
            )
        }
    }

    suspend fun getDeviceId(customerId: String) {
        val storedToken = token.first()
        if (storedToken != null) {
            val response = api.getDeviceId(
                token = "Bearer $storedToken",
                customerId = customerId
            )

            val deviceId = response.data[0].id.id

            credentialsDataStore.setDeviceId(deviceId)
        }
    }
}