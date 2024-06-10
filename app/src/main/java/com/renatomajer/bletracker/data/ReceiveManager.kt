package com.renatomajer.bletracker.data

import com.renatomajer.bletracker.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface ReceiveManager {

    val data: MutableSharedFlow<Resource<Data>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}