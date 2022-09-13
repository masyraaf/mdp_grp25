package com.group6.mdpandroid.entity

import java.sql.Timestamp
import java.util.*

class Message(val role: String, val message: String) {
    val time: Timestamp = Timestamp(Date().time)

    companion object {
        const val MESSAGE_RECEIVER = "You Received"
        const val MESSAGE_SENDER = "You Sent"
    }
}