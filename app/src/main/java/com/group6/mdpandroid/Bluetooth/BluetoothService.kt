package com.group6Android.mdp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.group6.mdpandroid.utils.Constants
import com.group6Android.mdp.bluetooth.BluetoothService.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothService(context: Context?, handler: Handler) {
    // Member fields

    private val TAG = "BluetoothService"

    private val mAdapter: BluetoothAdapter
    private val mHandler: Handler
    private var mSecureAcceptThread: AcceptThread? = null
    private var mInsecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private val mContext = context!!

    // Constants that indicate the current connection state
    val STATE_NONE = Constants.BLUETOOTH_STATE_NONE // we're doing nothing
    val STATE_LISTEN = Constants.BLUETOOTH_STATE_LISTENING // now listening for incoming connections
    val STATE_CONNECTING = Constants.BLUETOOTH_STATE_CONNECTING // now initiating an outgoing connection
    val STATE_CONNECTED = Constants.BLUETOOTH_STATE_CONNECTED // now connected to a remote device

    var connectionStatus : Boolean = false
    private lateinit var lastConnectedDevice : BluetoothDevice

    init {
        Log.d(TAG, "Is context null : ${(context==null)}")
    }

    /**
     * Return the current connection mstate.
     */
    @get:Synchronized
    var mstate: Int
        private set
    private var mNewState: Int

    /**
     * Update UI title according to the current mstate of the chat connection
     */
    @Synchronized
    private fun updateUserInterfaceTitle() {
        mstate = mstate
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mstate)
        mNewState = mstate

        // Give the new mstate to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget()
    }

    /**
     * Start the service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel()
//            mConnectThread = null
//        }
//
//        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {
//            mConnectedThread!!.cancel()
//            mConnectedThread = null
//        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = AcceptThread(true)
            mSecureAcceptThread!!.start()
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread(false)
            mInsecureAcceptThread!!.start()
        }

        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    @Synchronized
    fun connect(device: BluetoothDevice, secure: Boolean) {
        Log.d(TAG, "connect to: $device")
        lastConnectedDevice = device

        // Cancel any thread attempting to make a connection
        if (mstate == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device, secure)
        mConnectThread!!.start()

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice, socketType: String) {
        Log.d(TAG, "connected, Socket Type:$socketType")
        lastConnectedDevice = device

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }
        mstate = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            Log.d(TAG, "Sending out command: " + String(out!!))
            if (mstate != STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Unable to connect device")
        msg.data = bundle
        mHandler.sendMessage(msg)
        mstate = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Device connection was lost")
        msg.data = bundle
        mHandler.sendMessage(msg)
        mstate = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        connectionStatus = false

        // Start the service over to restart listening mode
        start()
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    @SuppressLint("MissingPermission")
    private inner class AcceptThread(secure: Boolean) : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?
        private val mSocketType: String
        override fun run() {
            Log.d(
                TAG, "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this
            )
            name = "AcceptThread$mSocketType"
            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (mstate != STATE_CONNECTED) {
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmServerSocket!!.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e)
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothService) {
                        when (mstate) {
                            STATE_LISTEN, STATE_CONNECTING ->                                 // Situation normal. Start the connected thread.
                                connected(
                                    socket, socket.remoteDevice,
                                    mSocketType
                                )
                            STATE_NONE, STATE_CONNECTED ->                                 // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(
                                        TAG,
                                        "Could not close unwanted socket",
                                        e
                                    )
                                }
                            else -> {}
                        }
                    }
                }
            }
            Log.i(
                TAG,
                "END mAcceptThread, socket Type: $mSocketType"
            )
        }

        fun cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this)
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e)
            }
        }


        init {
            var tmp: BluetoothServerSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Create a new listening server socket
            try {
                tmp = if (secure) {
                    // to continuously listen for incoming connections (from server(amd tool/ robot etc)
                    mAdapter.listenUsingRfcommWithServiceRecord(
                        NAME_SECURE,
                        MY_UUID_SECURE
                    )
                } else {
                    mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e)
            }
            mmServerSocket = tmp
            mstate = STATE_LISTEN
        }
    }


    /**
     * Return the current connection state.
     */
    @Synchronized
    fun getState(): Int {
        return mstate
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) :
        Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String
        override fun run() {
            Log.i(
                TAG,
                "BEGIN mConnectThread SocketType:$mSocketType"
            )
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(
                        TAG, "unable to close() " + mSocketType +
                                " socket during connection failure", e2
                    )
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {

            Log.d(TAG, "Cancelled Connectthread")
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "close() of connect $mSocketType socket failed", e
                )
            }
        }

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = if (secure) {
                    mmDevice.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE
                    )
                } else {
                    mmDevice.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
            }
            mmSocket = tmp
            mstate = STATE_CONNECTING
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(socket: BluetoothSocket?, socketType: String) :
        Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            connectionStatus = true

            // Keep listening to the InputStream while connected
            while (mstate == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget()

                    // check if incoming message is an image
                    if (isValidImage(buffer)) {
                        Log.d(TAG, "Found an image sent over bluetooth")
                    }
                    else {
                        val incomingmessage = String(buffer, 0, bytes)
                        Log.d(TAG, "IncomingMessage: $incomingmessage")


                        lateinit var action : String

                        if (incomingmessage.lowercase().contains("status")) {
                            // create intents variables
                            action = Constants.STATUS_UPDATE
                            broadcastMessage(mContext, action, incomingmessage)
                        }else if (incomingmessage.lowercase().contains("target")){
                            // create intents variables
                            action = Constants.TARGET_UPDATE
                            broadcastMessage(mContext, action, incomingmessage)
                        }else if(incomingmessage.lowercase().contains("robot")){
                            // create intents variables
                            action = Constants.ROBOT_UPDATE
                            broadcastMessage(mContext, action, incomingmessage)
                        }else{
                            action = Constants.MESSAGE_UPDATE
                            broadcastMessage(mContext, action, incomingmessage)
                            Log.d(TAG,"message received" )
                        }



                    }

                    val mIntent = Intent(Constants.BLUETOOTH_STATE)
                    mIntent.action = Constants.BLUETOOTH_STATE
                    mIntent.putExtra(Constants.BLUETOOTH_STATE, "Connected")
                    mContext.sendBroadcast(mIntent)

                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    val mIntent = Intent(Constants.BLUETOOTH_STATE)
                    mIntent.action = Constants.BLUETOOTH_STATE
                    mIntent.putExtra(Constants.BLUETOOTH_STATE, "Connection Lost")
                    mIntent.getStringExtra(Constants.BLUETOOTH_STATE)?.let { Log.d(TAG, "Intent Message (from ${TAG}) : ${ it }") }
                    mContext.sendBroadcast(mIntent)
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1,-1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }


        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }

        init {
            Log.d(
                TAG,
                "create ConnectedThread: $socketType"
            )
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            mstate = STATE_CONNECTED
        }
    }

    private fun isValidImage(byteArray: ByteArray): Boolean {
        try {
            val testImage: Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            return true
        } catch (e: Exception) {
            Log.d(TAG, e.localizedMessage)
            return false
        }
    }

    private fun broadcastMessage(mContext : Context, action : String, incomingMessage: String){
        val mIntent = Intent(action)
        mIntent.action = action
        mIntent.putExtra(action, incomingMessage)
        mContext.sendBroadcast(mIntent)
    }

    companion object {
        // Debugging
        private const val TAG = "BluetoothService"

        // Name for the SDP record when creating server socket
        private const val NAME_SECURE = "BluetoothChatSecure"
        private const val NAME_INSECURE = "BluetoothChatInsecure"
        private val MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var mConnectedThread: ConnectedThread? = null
        var BluetoothConnectionStatus = false

        /**
         * Write to the ConnectedThread in an unsynchronized manner
         *
         * @param out The bytes to write
         * @see ConnectedThread.write
         */
        fun write1(out: ByteArray?) {
            // Create temporary object
            var r: ConnectedThread
            // Synchronize a copy of the ConnectedThread
            // Perform the write unsynchronized
            Log.d(TAG, "write: Write Called.")
            mConnectedThread!!.write(out)
        }
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mstate = STATE_NONE
        mNewState = mstate
        mHandler = handler
    }
}