package com.renatomajer.bletracker.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatomajer.bletracker.data.DefaultRepository
import com.renatomajer.bletracker.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun getToken(username: String = "kristijan.horvat@fer.hr", password: String = "Mrkvaj123") {
        viewModelScope.launch {
            defaultRepository.getToken(username = username, password = password)
                .collect { resource ->

                    when (resource) {
                        is Resource.Loading -> {}

                        is Resource.Success -> {
                            Log.d("debug_log", resource.data.toString())
                            defaultRepository.storeRefreshToken(resource.data.refreshToken)
                            defaultRepository.storeToken(resource.data.token)
                            _canNavigate.value = true
                        }

                        is Resource.Error -> {
                            resource.error?.let {
                                if (it is HttpException && it.response()?.code() == 401) {
                                    Log.d("debug_log", "Wrong credentials")
                                    _canNavigate.value = false
                                }
                            }
                        }
                    }
                }
        }
    }
}