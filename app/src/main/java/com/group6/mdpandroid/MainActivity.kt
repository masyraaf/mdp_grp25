package com.group6.mdpandroid

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.group6.mdpandroid.adapters.ViewPagerAdapter
import com.group6.mdpandroid.utils.Constants
import com.group6Android.mdp.bluetooth.BluetoothService


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    lateinit var mBluetoothService: BluetoothService

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Controller"
                }
                1 -> {
                    tab.text = "Status"
                }
                2 -> {
                    tab.text = "Bluetooth"
                }
            }
        }.attach()
    }

    override fun onStart() {
        super.onStart()

        mBluetoothService = BluetoothService(this, mHandler)
        mBluetoothService.start()

        Log.d(TAG, "Is mBluetoothService Initialized : " + this::mBluetoothService.isInitialized)

    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    @SuppressLint("HandlerLeak")
    val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val activity = this
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    Constants.BLUETOOTH_STATE_CONNECTED -> {
                        Log.d(TAG, "Bluetooth Device connected")
                    }
                    Constants.BLUETOOTH_STATE_CONNECTING -> {
                        Log.d(TAG, "Bluetooth Device connecting...")
                    }
                    Constants.BLUETOOTH_STATE_LISTENING -> {
                        Log.d(TAG, "Bluetooth Service Listening")
                    }
                    Constants.BLUETOOTH_STATE_NONE -> {
                        Log.d(TAG, "Bluetooth Service state NONE")
                    }
                }
                // TODO: Not working, to fix
                Constants.MESSAGE_WRITE -> {
                    // NOTE : this only works when the bt device is actually connected, if not it never reaches the internal write state inside connectedThread
                    Log.d(TAG, "Writing a message from mHandler -MainActivity")
                }
                Constants.MESSAGE_READ -> {
                    Log.d(TAG, "Reading a message from mHandler -MainActivity")
                }
                Constants.MESSAGE_DEVICE_NAME -> {}
                Constants.MESSAGE_TOAST -> if (null != activity) {
                }
            }
        }
    }


}