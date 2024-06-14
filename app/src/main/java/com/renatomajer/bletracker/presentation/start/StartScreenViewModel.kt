package com.renatomajer.bletracker.presentation.start

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.getOrElse
import com.renatomajer.bletracker.data.DefaultRepository
import com.renatomajer.bletracker.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.nefilim.kjwt.JWT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val defaultRepository: DefaultRepository
) : ViewModel() {

    private val _canNavigate: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val canNavigate: StateFlow<Boolean> = _canNavigate.asStateFlow()

    var wrongCredentials = mutableStateOf(false)
        private set

    var isLoggingIn = mutableStateOf(false)
        private set

    fun getToken(username: String = "renato.majer@fer.hr", password: String = "Renato123") {
        isLoggingIn.value = true
        viewModelScope.launch {
            defaultRepository.getToken(username = username, password = password)
                .collect { resource ->

                    when (resource) {
                        is Resource.Loading -> {}

                        is Resource.Success -> {
                            Log.d("debug_log", resource.data.toString())

                            val token = resource.data.token

                            val customerId = parseJwtToCustomerId(token)

                            customerId?.let {
                                defaultRepository.getDeviceId(customerId)
                            }

                            defaultRepository.storeRefreshToken(resource.data.refreshToken)
                            defaultRepository.storeToken(token)
                            wrongCredentials.value = false
                            isLoggingIn.value = false
                            _canNavigate.value = true
                        }

                        is Resource.Error -> {
                            resource.error?.let {
                                if (it is HttpException && it.response()?.code() == 401) {
                                    Log.d("debug_log", "Wrong credentials")
                                    isLoggingIn.value = false
                                    wrongCredentials.value = true
                                    _canNavigate.value = false
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun parseJwtToCustomerId(token: String): String? {
        return when (val result = JWT.decode(token)) {
            is Either.Right -> {
                val customerId = result.value.jwt.claimValue("customerId").getOrElse { null }
                return customerId
            }

            is Either.Left -> {
                Log.e("JWT", "Error decoding JWT: ${result.value}")
                null
            }
        }
    }
}