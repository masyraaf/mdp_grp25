package com.group6.mdpandroid.entity

import android.bluetooth.BluetoothDevice

class Device(
    var device: BluetoothDevice,
    deviceName: String = "Unknown Device",
    var macAddr: String
) {

    /**
     * Class Variables
     */
    var deviceName: String = deviceName
        set(value) {
            field = if (value.isEmpty()) "Unknown Device" else value
        }
}