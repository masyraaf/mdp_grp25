package com.group6.mdpandroid.fragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group6.mdpandroid.MainActivity
import com.group6.mdpandroid.R
import com.group6.mdpandroid.adapters.BTDevicesAdapter
import com.group6.mdpandroid.databinding.FragmentBluetoothBinding
import com.group6.mdpandroid.entity.Device
import com.group6.mdpandroid.utils.Constants
import com.group6.mdpandroid.viewmodels.RobotMapViewModel
import com.group6Android.mdp.bluetooth.BluetoothService
import org.json.JSONObject
import kotlin.collections.ArrayList

class Bluetooth : Fragment() {

    val TAG = "BluetoothFragment"
    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!
    private var gridMap = GridMap()
    private var numOfObstacles = activity?.findViewById<TextView>(R.id.num_of_obstacles)

    /**
     * Local Bluetooth adapter
     */
    private lateinit var bluetoothAdapter: BluetoothAdapter
    /**
     * Member object for the bluetooth service
     */
    private lateinit var mbluetoothService: BluetoothService

    // Recycler View will show all paired devices
    private lateinit var BTRecyclerView : RecyclerView
    private var bondedDeviceList : ArrayList<Device> = ArrayList()
    private var newDevicesList: ArrayList<Device> = ArrayList()
    private var discoveredDevices: HashSet<BluetoothDevice> = hashSetOf<BluetoothDevice>()
    private lateinit var mBTDevicesAdapter : BTDevicesAdapter

    // Intent request codes
    private val REQUEST_CONNECT_DEVICE_SECURE = 1
    private val REQUEST_CONNECT_DEVICE_INSECURE = 2
    private val REQUEST_ENABLE_BT = 3

    private val viewModel: RobotMapViewModel by activityViewModels()

    lateinit var lastConnectedDevice  : BluetoothDevice

    var retryConnection = false
    var reconnectionHandler = Handler()
    var reconnectionRunnable: Runnable = object : Runnable {
        override fun run() {
            // Magic here
            try {
                if (!mbluetoothService.connectionStatus) {
                    mbluetoothService.getState()
                    mbluetoothService.connect(
                        lastConnectedDevice,
                        false
                    )
                    Log.d(TAG, "Reconnection Success")
                }
                reconnectionHandler.removeCallbacks(this)
                retryConnection = false
            } catch (e: Exception) {
                Log.d(
                    TAG,
                    "Failed to reconnect, reconnecting in 5 second"
                )
            }
        }
    }

    /**
     * String buffer for outgoing messages
     */
    private lateinit var mOutStringBuffer: StringBuffer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!bluetoothAdapter.isEnabled()) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else if (!this::mbluetoothService.isInitialized) {
            setupBluetooth()
        }


        //TODO: To replace with dynamic data stored in viewmodel
        //gets paired devices. (but only updated during start up, doesnt get updated dynamically when a new device paired)
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        for (device: BluetoothDevice in pairedDevices!!) {
//            bondedDeviceList.add(Device(device, device.name, device.address))
            newDevicesList.add(Device(device, device.name, device.address))
            discoveredDevices.add(device)
        }

//        mBTDevicesAdapter = BTDevicesAdapter(view.context, bondedDeviceList, mbluetoothService)
        mBTDevicesAdapter = BTDevicesAdapter(view.context, newDevicesList, mbluetoothService)
        BTRecyclerView = view.findViewById(R.id.devicesList)
        BTRecyclerView.adapter = mBTDevicesAdapter
        BTRecyclerView.layoutManager = LinearLayoutManager(view.context)


        // bind buttons to actions
        binding.onOffBtn.setOnClickListener() { view ->
            //Turn bluetooth on/off
            Log.v("SR", "onoffbtn clicked");
            if (!bluetoothAdapter.isEnabled) {
                val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetooth, 1)
                Toast.makeText(activity, "Bluetooth Turned ON", Toast.LENGTH_SHORT).show()

            } else {
                bluetoothAdapter.disable()
                Toast.makeText(activity, "Bluetooth Turned OFF", Toast.LENGTH_SHORT).show()
            }
        }
        binding.discoverableBtn.setOnClickListener() { view ->
            //Set as discoverable
            Log.v("SR", "discoverable clicked")
            if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
            ) {
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                startActivity(discoverableIntent)
                Toast.makeText(activity, "Device now discoverable", Toast.LENGTH_SHORT).show()
            }
        }

        // this part supposed to discover devices, but nothing is implemented. we are only starting discovery
        // -> not outputting it anywhere on the device.(so that we can connect)
        // so far the connection is implemeneted through remote side (aka robot/rpi/amd tool)
        binding.refreshBtn.setOnClickListener { view ->

            Log.v("SR", "refresh clicked")
            // If we're already discovering, stop it
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery()

            }else{
                bluetoothAdapter.startDiscovery()
                Toast.makeText(view.context, "Now Scanning", Toast.LENGTH_SHORT).show()
            }
            // Here, thisActivity is the current activity
//            if (ContextCompat.checkSelfPermission(requireActivity(),
//                    android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                // Permission is not granted
//                // Should we show an explanation?
//                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
//                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    // Show an explanation to the user *asynchronously* -- don't block
//                    // this thread waiting for the user's response! After the user
//                    // sees the explanation, try again to request the permission.
//                } else {
//                    // No explanation needed, we can request the permission.
//                    ActivityCompat.requestPermissions(requireActivity(),
//                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                        1)
//
//                    // REQUEST_CODE is an
//                    // app-defined int constant. The callback method gets the
//                    // result of the request.
//                }
//                toggleDiscovery(view)
//            } else {
//                // Permission has already been granted
//
////                Log.v("SR", "refresh clicked")
////                // If we're already discovering, stop it
////                if (bluetoothAdapter.isDiscovering()) {
////                    bluetoothAdapter.cancelDiscovery()
////
////                }else{
////                    bluetoothAdapter.startDiscovery()
////                    Toast.makeText(view.context, "Now Scanning", Toast.LENGTH_SHORT).show()
////                }
//                toggleDiscovery(view)
//            }



        }


        binding.sendBtn.setOnClickListener() { view ->
            Log.v("SR", "Clicked on sendBtn")

            // retrieve text from text input
            val msg: String = binding.textInput.text.toString();
            binding.textInput.text?.clear();
            mbluetoothService.write(msg.toByteArray());

        }
    }

    @SuppressLint("MissingPermission")
    private fun toggleDiscovery(view: View){
        Log.v("SR", "refresh clicked")
        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery()

        }else{
            bluetoothAdapter.startDiscovery()
            Toast.makeText(view.context, "Now Scanning", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // get gridMap fragment class
        gridMap = (activity?.supportFragmentManager?.findFragmentById(R.id.fragmentContainerView4) as GridMap?)!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val filter = IntentFilter().apply {
            // information about the each device discovered. ACTION_FOUND INTENT RQUIRED.
            // so far nothing is being done if device is discovered. should add.
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(Constants.STATUS_UPDATE)
            addAction(Constants.TARGET_UPDATE)
            addAction(Constants.ROBOT_UPDATE)
        }
        // METHOD to be able to receive information about the each device discovered.
        requireActivity().registerReceiver(receiver, filter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }

    /**androi
     * Set up the UI and background operations for chat.
     */
    private fun setupBluetooth(){
        Log.d(TAG, "Setting up bluetoothService")
        // Initialize the array adapter for the conversation thread
        val activity = activity
        // Initialize the BluetoothChatService to perform bluetooth connections

        mbluetoothService = (activity as MainActivity).mBluetoothService
//        viewModel.addStatusText("Bluetooth Status Code :" + mbluetoothService.mstate)
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = StringBuffer()
    }


    val receiver = object: BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val device: BluetoothDevice?
            val getCurrentConnection: String?
            //val totalNumberOfObstacles = Integer.parseInt(numOfObstacles?.text.toString())
            Log.d(TAG, action!!)

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (!discoveredDevices.contains(device)){
                        val name = if (device!!.name == null) "PLACEHOLDER_NAME" else device.name
                        Log.d(TAG, "device added: " + name)
                        newDevicesList.add(Device(device!!, name, device.address))
                        discoveredDevices.add(device!!)
                        BTRecyclerView.adapter?.notifyItemInserted(newDevicesList.size)
                        Log.d(TAG, "device added: " + name)
                    }

                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    lastConnectedDevice = device!!
                    Log.d(TAG, "Connected to bluetooth device : " + device.name)
                    viewModel.addStatusText("Connected to bluetooth device : " + device.name)
                    Toast.makeText(requireContext(), "Connected to bluetooth device : " + device.name, Toast.LENGTH_SHORT).show()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    // TODO reconnect
                    retryConnection = true
                    reconnectionHandler.postDelayed(reconnectionRunnable, 5000)
                    Toast.makeText(requireContext(), "Disconnected from bluetooth device" , Toast.LENGTH_SHORT).show()
                }
                Constants.STATUS_UPDATE -> {
                    val incomingMessage = intent.getStringExtra(Constants.STATUS_UPDATE)!!
                    var json = JSONObject(incomingMessage)
                    val status = json.getString("status")
//                    var result = incomingMessage.substringAfter(Constants.STATUS_UPDATE).substringBefore("|")
                    //Log.d(TAG, "Truncated Message : $result")
                    viewModel.addStatusText(status)
                }
                Constants.TARGET_UPDATE -> {
                    val incomingMessage = intent.getStringExtra(Constants.TARGET_UPDATE)!!
                    // TODO : Confirm that this works

                    var result = incomingMessage.substringAfter(Constants.TARGET_UPDATE).substringBefore("|")
                    //Log.d(TAG, "Truncated Message $result")
                    viewModel.addStatusText(result)

                    val stringSplit: List<String> = result.split(",")
                    val obstacleNumber = Integer.parseInt(stringSplit[1])
                    val targetID = Integer.parseInt(stringSplit[2])

                    // Retrieve xPos and yPos of seen image and update GridMap with targetID
                    val xPos = viewModel.arrayOfGridPoints[obstacleNumber].xPos
                    val yPos = viewModel.arrayOfGridPoints[obstacleNumber].yPos
                    val obstacleDirection = viewModel.arrayOfGridPoints[obstacleNumber].value

                    Log.v("fml", "at obstacle: $obstacleNumber")
                    Log.v("fml", "number of obstacles:" + viewModel.arrayOfGridPoints.size.toString())
                    gridMap.updateObstacleImage(xPos, yPos, targetID, obstacleNumber, obstacleDirection)

                }
                Constants.ROBOT_UPDATE -> {
                    val incomingMessage = intent.getStringExtra(Constants.ROBOT_UPDATE)!!
                    // TODO : Confirm that this works

                    val result = incomingMessage.substringAfter(Constants.ROBOT_UPDATE).substringBefore("|")
                    viewModel.addStatusText(result)

                    val stringSplit: List<String> = result.split(",")

                    val robotX = Integer.parseInt(stringSplit[1])
                    val robotY = Integer.parseInt(stringSplit[2])
                    val robotDirection = stringSplit[3]

                    Log.d(TAG, "x : ${robotX}, y: ${robotY}, robotDirection : $robotDirection")
                    gridMap.updateRobotPositionAndFacingDirection(robotX, robotY, robotDirection)
                }

                else -> Log.d(TAG, "Default case for receiver")
            }
        }
    }

}