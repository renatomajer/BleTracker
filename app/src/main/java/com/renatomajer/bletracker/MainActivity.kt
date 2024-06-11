package com.renatomajer.bletracker

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.renatomajer.bletracker.presentation.Navigation
import com.renatomajer.bletracker.ui.theme.BleTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private var isBluetoothDialogAlreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            BleTrackerTheme {
                Navigation(
                    onBluetoothStateChanged = {
                        showBluetoothDialog()
                    }
                )
            }
        }
    }


    override fun onStart() {
        super.onStart()

        showBluetoothDialog()
    }

    private fun showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled) {
            if (!isBluetoothDialogAlreadyShown) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
                isBluetoothDialogAlreadyShown = true
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isBluetoothDialogAlreadyShown = false
            if (result.resultCode != Activity.RESULT_OK) {
                showBluetoothDialog()
            }
        }
}