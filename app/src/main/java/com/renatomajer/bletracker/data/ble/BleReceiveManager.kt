package com.renatomajer.bletracker.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.renatomajer.bletracker.data.ConnectionState
import com.renatomajer.bletracker.data.Data
import com.renatomajer.bletracker.data.ReceiveManager
import com.renatomajer.bletracker.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEVICE_NAME = "esp32_mrkvaj"
private const val DEVICE_ADDRESS = "4C:11:AE:DE:8D:56"

@SuppressLint("MissingPermission")
class BleReceiveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : ReceiveManager {
    override val data: MutableSharedFlow<Resource<Data>> = MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(
                "BleReceiveManager",
                "Pronađen uređaj: ${result.device.address}, ${result.device.name}"
            )

//            coroutineScope.launch {
//                data.emit(Resource.Loading(message = "${result.device.name}"))
//            }
            if (result.device.address == DEVICE_ADDRESS) {
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device..."))
                }

                if (isScanning) {
                    Log.d("BleReceiveManager", "Pokušaj spajanja na uređaj")
                    result.device.connectGatt(
                        context,
                        false,
                        gattCallback,
                        BluetoothDevice.TRANSPORT_LE
                    )
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private val MAX_CONNECTION_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BleReceiveManager", "Povezivanje uspješno")

                    coroutineScope.launch {
                        //data.emit(Resource.Loading(message = "Discovering Services..."))
                        data.emit(
                            Resource.Success(
                                Data(
                                    data = "Connected to device",
                                    connectionState = ConnectionState.Connected
                                )
                            )
                        )
                    }
                    //gatt.discoverServices()
                    this@BleReceiveManager.gatt = gatt

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BleReceiveManager", "Veza prekinuta")

                    coroutineScope.launch {
                        data.emit(
                            Resource.Success(
                                data = Data(
                                    data = "Disconnected from device",
                                    connectionState = ConnectionState.Disconnected
                                )
                            )
                        )
                    }
                    gatt.close()
                }
            } else { // Not success
                Log.d("BleReceiveManager", "Spajanje nije uspjelo s statusom: $status")

                gatt.close()
                currentConnectionAttempt += 1

                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Attempting to connect $currentConnectionAttempt/$MAX_CONNECTION_ATTEMPTS"))
                }

                if (currentConnectionAttempt <= MAX_CONNECTION_ATTEMPTS) {
                    startReceiving()
                } else {
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to ble device"))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            with(gatt) {
                printGattTable()

                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Adjusting MTU space..."))
                }

                gatt.requestMtu(517)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            // TODO: remove or implement
        }
    }

    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning Ble devices..."))
        }

        if (!bluetoothAdapter.isEnabled) {
            coroutineScope.launch {
                data.emit(Resource.Error(errorMessage = "Bluetooth nije dostupan ili nije uključen"))
            }
            return
        }

        isScanning = true
        Log.d("BleReceiveManager", "startReceiving: Početak skeniranja")
        val filter = ScanFilter.Builder().setDeviceAddress(DEVICE_ADDRESS).build()
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)

        gatt?.close()
    }
}