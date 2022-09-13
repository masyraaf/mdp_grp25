package com.group6.mdpandroid.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.group6.mdpandroid.R
import com.group6.mdpandroid.entity.Device
import com.group6Android.mdp.bluetooth.BluetoothService

class BTDevicesAdapter(
    context: Context,
    private val deviceList: ArrayList<Device>,
    private val mBluetoothService: BluetoothService
) : RecyclerView.Adapter<BTDevicesAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    private var ct = context
    private val TAG = "BTDevicesAdapter"
    private val mmBluetoothService = mBluetoothService


    // to create dynamic lists
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val TAG = "ViewHolder"

        var device: TextView? = null
        var macAddr: TextView? = null

        init {
            // Define click listener for the ViewHolder's View.
            device = view.findViewById(R.id.device_name)
            Log.d(TAG, device.toString())
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(ct)
            .inflate(R.layout.bt_device_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.device?.text = deviceList[i].deviceName
        viewHolder.macAddr?.text = deviceList[i].macAddr

        viewHolder.device?.setOnClickListener() {
            Log.v(TAG, viewHolder.device?.text as String)
            mmBluetoothService.connect(deviceList[i].device, false)
            Toast.makeText(ct, "Connecting to ${deviceList[i].device.name}", Toast.LENGTH_SHORT).show()
        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = deviceList.size


}