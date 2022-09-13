package com.group6.mdpandroid.fragments

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.group6.mdpandroid.MainActivity
import com.group6.mdpandroid.databinding.FragmentStatusDisplayBinding
import com.group6.mdpandroid.viewmodels.RobotMapViewModel
import com.group6Android.mdp.bluetooth.BluetoothService
import kotlin.properties.Delegates


class StatusDisplay : Fragment() {

    // Initialise binding, view model
    private var _binding: FragmentStatusDisplayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RobotMapViewModel by activityViewModels()

    // Initialise variables
    private var robotPosTextView: TextView? = null
    private var robotDirTextView: TextView? = null
    private var statusLogTextView: LinearLayout? = null
    private lateinit var chronometer: Chronometer

    /**
     * Member object for the bluetooth service
     */
    private lateinit var mbluetoothService: BluetoothService

    // Depends on variable timerStarted in RobotMapViewModel which is toggled by RobotController
    // When this changes, chronometer starts to runs
    private var timerStarted: Boolean by Delegates.observable(false) { _, _, _ ->
        if (timerStarted) {
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
        }
//        else {
//            chronometer.base = SystemClock.elapsedRealtime()
//            chronometer.stop()
//        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatusDisplayBinding.inflate(inflater, container, false)
        robotPosTextView = binding.robotPosText
        robotDirTextView = binding.robotDirText
        chronometer = binding.chronometer
        statusLogTextView = binding.statusLog

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Set up observers
        viewModel.robotPosition.observe(viewLifecycleOwner, Observer { posArray ->
            robotPosTextView?.text = "X: ${posArray[0]}, Y: ${posArray[1]}"
        })
        viewModel.robotDirection.observe(viewLifecycleOwner) { string ->
            robotDirTextView?.text = string
        }
        viewModel.timeStarted.observe(viewLifecycleOwner) { started ->
            timerStarted = started
        }
        viewModel.statusTexts.observe(viewLifecycleOwner) { string ->
            val sampleView = TextView(activity)
            sampleView.text = string
            sampleView.textSize = 24f
            binding.statusLog.addView(sampleView)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::mbluetoothService.isInitialized) {
            mbluetoothService = (activity as MainActivity).mBluetoothService
        }
    }

}