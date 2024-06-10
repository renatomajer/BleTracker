package com.renatomajer.bletracker.presentation

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatomajer.bletracker.data.ConnectionState
import com.renatomajer.bletracker.data.ReceiveManager
import com.renatomajer.bletracker.data.ble.BleReceiveManager
import com.renatomajer.bletracker.util.Resource
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val manager =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = manager.adapter


    private val receiveManager: ReceiveManager = BleReceiveManager(
        context = application.applicationContext,
        bluetoothAdapter = bluetoothAdapter
    )

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)
        private set

    var data by mutableStateOf<String?>(null)

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

    override fun onCleared() {
        super.onCleared()
        receiveManager.closeConnection()
    }
}