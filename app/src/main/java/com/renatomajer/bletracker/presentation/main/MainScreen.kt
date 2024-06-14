package com.renatomajer.bletracker.presentation.main

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.renatomajer.bletracker.data.ConnectionState
import com.renatomajer.bletracker.presentation.MainActivityViewModel
import com.renatomajer.bletracker.presentation.permissions.PermissionsUtils
import com.renatomajer.bletracker.presentation.permissions.SystemBroadcastReceiver

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = hiltViewModel(),
    onBluetoothStateChanged: () -> Unit
) {

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver

        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }

    val permissionState =
        rememberMultiplePermissionsState(permissions = PermissionsUtils.permissions)

    val lifecycleOwner = LocalLifecycleOwner.current

    val bleConnectionState = viewModel.connectionState

    LaunchedEffect(key1 = true) {
        // Fetch stealing state
        viewModel.startPolling()
    }

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_START) {
                permissionState.launchMultiplePermissionRequest()
//                if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
//                    // Case when the user already had the connection and minimizes the app -> ON_STOP the device will disconnect
//                    // and when the user reopens the app -> ON_START gets fired, we will reconnect
//                    viewModel.reconnect()
//                }
            }

            if (event == Lifecycle.Event.ON_STOP) {
                // This closes the connection when the user closes the app
//                if (bleConnectionState == ConnectionState.Connected) {
//                    viewModel.disconnect()
//                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

//    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
//        if (permissionState.allPermissionsGranted) {
//            if (bleConnectionState == ConnectionState.Uninitialized) {
//                viewModel.initializeConnection()
//            }
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (viewModel.isStealing) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Someone is riding your bike!",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
                .border(
                    BorderStroke(
                        5.dp, Color.Blue
                    ),
                    RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (bleConnectionState == ConnectionState.CurrentlyInitializing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    if (viewModel.initializingMessage != null) {
                        Text(
                            text = viewModel.initializingMessage!!
                        )
                    }
                }
            } else if (!permissionState.allPermissionsGranted) {
                Text(
                    text = "Go to the app setting and allow the missing permissions.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
                )
            } else if (viewModel.errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.errorMessage!!
                    )
                    Button(
                        onClick = {
                            if (permissionState.allPermissionsGranted) {
                                viewModel.initializeConnection()
                            }
                        }
                    ) {
                        Text(
                            "Try again"
                        )
                    }
                }
            } else if (bleConnectionState == ConnectionState.Connected) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${viewModel.data}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (bleConnectionState == ConnectionState.Disconnected) {
                Button(
                    onClick = {
                        viewModel.initializeConnection()
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color(0xFFFB9927)
                    )
                ) {
                    Text("Initialize again")
                }
            } else if (bleConnectionState == ConnectionState.Uninitialized) {
                Button(
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color(0xFFFB9927)
                    ),
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            viewModel.initializeConnection()
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unlock")
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
                    }
                }
            }
        }

        if (viewModel.isStealing) {
            Spacer(modifier = Modifier.height(50.dp))

            val telemetry = viewModel.telemetry
            val values = telemetry?.location?.getOrNull(0)?.value?.split(", ")

            val lat = values?.getOrNull(0)?.toDouble()
            val lon = values?.getOrNull(1)?.toDouble()

            // Show map
            if (lat != null && lon != null) {

                val bikeLocation = LatLng(lat, lon)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(bikeLocation, 10f)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Check your bike location:", fontWeight = FontWeight.Bold)
                }

                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = bikeLocation),
                            title = "Bike location",
                            snippet = "Marker on bike location"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.stopPolling()
                },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color(0xFFFB9927)
                )
            )
            {
                Text(text = "Cancel stealing notification")
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}