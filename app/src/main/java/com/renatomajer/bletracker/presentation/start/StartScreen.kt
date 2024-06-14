package com.renatomajer.bletracker.presentation.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.renatomajer.bletracker.R
import com.renatomajer.bletracker.presentation.Screen

@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: StartScreenViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val canNavigate by viewModel.canNavigate.collectAsState()

    LaunchedEffect(key1 = canNavigate) {
        if (canNavigate) {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(Screen.StartScreen.route) {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Image(
            modifier = Modifier.height(60.dp),
            painter = painterResource(id = R.drawable.carrot),
            contentDescription = null
        )

        if (viewModel.isLoggingIn.value) {
            CircularProgressIndicator()

        } else {
            Column {
                if (viewModel.wrongCredentials.value) {
                    Text(text = "Wrong credentials", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(text = "username")
                    },
                    isError = viewModel.wrongCredentials.value
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(text = "password")
                    },
                    isError = viewModel.wrongCredentials.value,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        }

        Button(
            onClick = {
                viewModel.getToken(username = username, password = password)
            },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color(0xFFFB9927)
            )
        ) {
            Text(
                text = "Login",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}