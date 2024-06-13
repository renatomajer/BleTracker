package com.renatomajer.bletracker.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatomajer.bletracker.data.ConnectionState
import com.renatomajer.bletracker.data.DefaultRepository
import com.renatomajer.bletracker.data.ReceiveManager
import com.renatomajer.bletracker.data.remote.dto.Telemetry
import com.renatomajer.bletracker.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val receiveManager: ReceiveManager,
    private val defaultRepository: DefaultRepository
) : ViewModel() {

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)
        private set

    var data by mutableStateOf<String?>(null)

    var pollingJob: Job? = null

    var telemetry by mutableStateOf<Telemetry?>(null)
        private set

    var isStealing by mutableStateOf<Boolean>(false)
        private set

    var pollingDelay = 10000L

    private fun listenForChanges() {
        viewModelScope.launch {
            receiveManager.data.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        data = result.data.data
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect() {
        receiveManager.disconnect()
    }

    fun reconnect() {
        receiveManager.reconnect()
    }

    fun initializeConnection() {
        errorMessage = null
        listenForChanges()
        receiveManager.startReceiving()
    }

    fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (true) {
                defaultRepository.getTelemetry().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {

                            val telemetry: Telemetry = resource.data
                            val status = telemetry.status[0].value

                            // check if status is "stealing"
                            // if it is, start polling and change screen status
                            // set isStealing to true
                            if (status == "1" || status == "2") {
                                this@MainActivityViewModel.telemetry = resource.data
                                isStealing = true
                                pollingDelay = 5000L
                            }

                            // if the telemetry status is "not stealing", it means that the stealing has been stopped
                            // update isStealing to false and stopPolling
                            if (status == "0") {
                                isStealing = false
//                                pollingJob?.cancel()
//                                pollingJob = null
                                pollingDelay = 10000L
                            }
                        }

                        is Resource.Loading -> {}

                        is Resource.Error -> {
                            Log.d("debug_log", resource.error.toString())
                        }
                    }
                }
                delay(pollingDelay)
            }
        }
    }

    fun stopPolling() {
        viewModelScope.launch {
            defaultRepository.stopStealing()
        }
    }

    override fun onCleared() {
        super.onCleared()
        receiveManager.closeConnection()
    }
}