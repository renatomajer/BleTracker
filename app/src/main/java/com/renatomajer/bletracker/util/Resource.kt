package com.renatomajer.bletracker.util

sealed class Resource<out T : Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Error(val errorMessage: String, val error: Throwable? = null) : Resource<Nothing>()
    data class Loading<out T : Any>(val data: T? = null, val message: String? = null) :
        Resource<T>()
}