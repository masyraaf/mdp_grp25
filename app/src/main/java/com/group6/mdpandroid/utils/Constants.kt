package com.group6.mdpandroid.utils

interface Constants {
    companion object {
        const val GRID_HEIGHT = 20
        const val GRID_WIDTH = 20
        const val INFINITE_COST = Integer.MAX_VALUE

        // Constants for A*
        const val MOVE_ONE_BLOCK = 1

        // Constants for task selection
        const val NO_TASK_SELECTED = 0
        const val IMAGE_RECOGNITION = 1
        const val NAVIGATE_OBSTACLES = 2

        // Strings for robot commands
        const val upCmd = "w"
        const val downCmd = "x"
        const val onTheSpotLeft = "t"
        const val onTheSpotRight = "y"
        const val upLeftCmd = "q"
        const val upRightCmd = "e"
        const val downLeftCmd = "z"
        const val downRightCmd = "c"
        const val stopCmd = "s"
        const val takePic = "PIC"

        const val DEFAULT_DISTANCE = 10
        const val DEFAULT_ANGLE = 90

        // Constants for Bluetooth Service
        var MESSAGE_STATE_CHANGE = 1
        var MESSAGE_READ = 2
        var MESSAGE_WRITE = 3
        var MESSAGE_DEVICE_NAME = 4
        var MESSAGE_TOAST = 5

        // Key names received from the BluetoothService Handler
        var DEVICE_NAME = "device_name"
        var TOAST = "toast"

        // Constants that indicate the current connection state
        const val BLUETOOTH_STATE_NONE = 6 // we're doing nothing
        const val BLUETOOTH_STATE_LISTENING = 7 // now listening for incoming connections
        const val BLUETOOTH_STATE_CONNECTING = 8 // now initiating an outgoing connection
        const val BLUETOOTH_STATE_CONNECTED = 9 // now connected to a remote device

        const val STATUS_UPDATE = "STATUS"
        const val TARGET_UPDATE = "TARGET"
        const val ROBOT_UPDATE = "ROBOT"
        const val INVALID_CODE = -1
        const val BLUETOOTH_STATE = "BluetoothState"
        const val MESSAGE_UPDATE = "MESSAGE"


        enum class GridPointType {
            EMPTY,
            ROBOT_CENTER,
            ROBOT_OTHER,
            ROBOT_TOP,
            ROBOT_LEFT,
            ROBOT_RIGHT,
            ROBOT_BOTTOM,
            ROBOT_ONTHESPOTLEFT,
            ROBOT_ONTHESPOTRIGHT,
            OBSTACLE_TOP,
            OBSTACLE_BOTTOM,
            OBSTACLE_LEFT,
            OBSTACLE_RIGHT,
            OBSTACLE,
            EDGE
        }

    }
}