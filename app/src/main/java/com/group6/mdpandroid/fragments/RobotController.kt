package com.group6.mdpandroid.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.group6.mdpandroid.MainActivity
import com.group6.mdpandroid.R
import com.group6.mdpandroid.algorithm.*
import com.group6.mdpandroid.databinding.FragmentRobotControllerBinding
import com.group6.mdpandroid.entity.GridPoint
import com.group6.mdpandroid.utils.CommandGenerator
import com.group6.mdpandroid.utils.Constants.Companion.downCmd
import com.group6.mdpandroid.utils.Constants.Companion.upLeftCmd
import com.group6.mdpandroid.utils.Constants.Companion.upRightCmd
import com.group6.mdpandroid.utils.Constants.Companion.upCmd
import com.group6.mdpandroid.utils.Constants.Companion.DEFAULT_DISTANCE
import com.group6.mdpandroid.utils.Constants.Companion.downLeftCmd
import com.group6.mdpandroid.utils.Constants.Companion.downRightCmd
import com.group6.mdpandroid.utils.Constants.Companion.onTheSpotLeft
import com.group6.mdpandroid.utils.Constants.Companion.onTheSpotRight
import com.group6.mdpandroid.utils.Constants.Companion.takePic
import com.group6.mdpandroid.viewmodels.RobotMapViewModel
import com.group6Android.mdp.bluetooth.BluetoothService
import java.util.*

class RobotController : Fragment() {
    var obstacleIndex: Int = 0
    private val TAG = "RobotController"

    // Initialise binding, view model
    private var _binding: FragmentRobotControllerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RobotMapViewModel by activityViewModels()
    private var gridMap = GridMap()
    private lateinit var gridDetails: GridDetails
    private var realRun: Boolean = false
    var handler: Handler = Handler(Looper.getMainLooper())
    val commandsQueue: Queue<String> = LinkedList()

    /**
     * Member class that will handle bluetooth comms
     * This object will be initialized from MainActivity, this fragment only
     * has to call the write and read methods
     */
    private lateinit var mbluetoothService : BluetoothService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::mbluetoothService.isInitialized) {
            mbluetoothService = (activity as MainActivity).mBluetoothService
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRobotControllerBinding.inflate(inflater, container, false)

        // Set event listeners for directional pad
        binding.buttonForwardLeft.setOnClickListener {
            if (viewModel.robotDirection.value != null){
                viewModel.setLeftForwardToggle()
                viewModel.addStatusText("ROBOT: Left Forward")

                var cmd : String = upLeftCmd
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on forward left button")
        }
        binding.buttonUp.setOnClickListener {
            if (viewModel.robotDirection.value != null){
                viewModel.setUpToggle()
                viewModel.addStatusText("ROBOT: Up")
                var cmd : String = upCmd
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                Log.d(TAG, "Clicked on up button here")
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on up button")
        }
        binding.buttonForwardRight.setOnClickListener {
            if (viewModel.robotDirection.value != null){
                viewModel.setRightForwardToggle()
                viewModel.addStatusText("ROBOT: Right Forward")

                var cmd : String = upRightCmd
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on forward right button")
        }
        binding.buttonOnTheSpotLeft.setOnClickListener{
            if (viewModel.robotDirection.value != null){
                viewModel.setOnTheSpotLeftToggle()
                viewModel.addStatusText("ROBOT: On-the-spot Left")

                var cmd : String = onTheSpotLeft
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on back left button")
        }
        binding.buttonOnTheSpotRight.setOnClickListener{
            if (viewModel.robotDirection.value != null){
                viewModel.setOnTheSpotRightToggle()
                viewModel.addStatusText("ROBOT: On-the-spot Right")

                var cmd : String = onTheSpotRight
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on back left button")
        }
        binding.buttonBackLeft.setOnClickListener {
            if (viewModel.robotDirection.value != null){
                viewModel.setLeftBackToggle()
                viewModel.addStatusText("ROBOT: Left Backwards")

                var cmd : String = downLeftCmd
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on back left button")
        }
        binding.buttonDown.setOnClickListener {
            if (viewModel.robotDirection.value != null){
                viewModel.setDownToggle()
                viewModel.addStatusText("ROBOT: Down")
                var cmd : String = downCmd
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on down button")
        }
        binding.buttonBackRight.setOnClickListener {
            if (viewModel.robotDirection.value != null){
                viewModel.setRightBackToggle()
                viewModel.addStatusText("ROBOT: Right Backwards")

                var cmd : String = downRightCmd
                cmd = CommandGenerator.generateCommand(direction = cmd, distance = null, angle = null, index = null, leftRight = null)

                Log.d(TAG, cmd)
                mbluetoothService.write(cmd.toByteArray())
            }

            Log.d(TAG, "Clicked on back right button")
        }

        binding.mapConfig1.setOnClickListener {
            Toast.makeText(activity, "Custom Map Config 1 loaded!", Toast.LENGTH_SHORT).show()
            gridMap.mapConfig1()
        }

        binding.mapConfig2.setOnClickListener {
            Toast.makeText(activity, "Custom Map Config 2 loaded!", Toast.LENGTH_SHORT).show()
            gridMap.mapConfig2()
        }

        binding.runTask.setOnClickListener {
            viewModel.setTimeStarted(true)

            // Convert map details into grid details required for algo
            gridDetails = GridDetails(viewModel.arrayOfGridPoint)

            // Run algorithm 3
            val algorithm = HamiltonianAlgo3(gridDetails)
            var (pathToTake, orderedObstacles) = algorithm.runImageRecognitionTask()
            var reordered: ArrayList<GridPoint> = ArrayList<GridPoint>()

            // Add the obstacles in the order that they are to be visited
            for (queueItem in orderedObstacles) {
                for (arrayListItem in viewModel.arrayOfGridPoints) {
                    if (arrayListItem.xPos == queueItem.getIndexColumn() &&
                        arrayListItem.yPos == queueItem.getIndexRow()) {
                        reordered.add(arrayListItem)
                    }
                }
            }

            // Remove original unordered obstacles
            viewModel.arrayOfGridPoints.removeAll(viewModel.arrayOfGridPoints)
            viewModel.arrayOfGridPoints.addAll(reordered)

            convertToCommands(pathToTake, orderedObstacles)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // get gridMap fragment class
        gridMap = (activity?.supportFragmentManager?.findFragmentById(R.id.fragmentContainerView4) as GridMap?)!!
    }


    private fun checkIfCloseRight(obstacle: ArenaCell): Boolean{

        var xPos = obstacle.getIndexColumn()
        var yPos = obstacle.getIndexRow()
        var direction = obstacle.getImageDirection()

        var cellArray: ArrayList<ArenaCell> = ArrayList<ArenaCell>()

        if (direction == Direction.SOUTH){
            if (xPos == 17){
                cellArray.add(gridDetails.getArenaCell(xPos+2, yPos))
                if (yPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos - 1))
                } else if (yPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos + 1))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos + 1))
                }
            }
            else if (xPos < 17) {
                cellArray.add(gridDetails.getArenaCell(xPos + 2, yPos))
                cellArray.add(gridDetails.getArenaCell(xPos + 3, yPos))
                if (yPos < 19){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos + 1))
                    cellArray.add(gridDetails.getArenaCell(xPos+3, yPos + 1))

                if (yPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos+3, yPos - 1))
                }
            }}
        }
        else if (direction == Direction.NORTH){
            if (xPos == 2){
                cellArray.add(gridDetails.getArenaCell(xPos-2, yPos))
                if (yPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos - 1))
                } else if (yPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos + 1))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos + 1))
                }
            }
            else if (xPos > 2) {
                cellArray.add(gridDetails.getArenaCell(xPos - 2, yPos))
                cellArray.add(gridDetails.getArenaCell(xPos - 3, yPos))
                if (yPos < 19) {
                    cellArray.add(gridDetails.getArenaCell(xPos - 2, yPos + 1))
                    cellArray.add(gridDetails.getArenaCell(xPos - 3, yPos + 1))
                }
                if (yPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos-3, yPos - 1))
                }
            }
        }
        else if (direction == Direction.WEST){
            if (yPos == 2){
                cellArray.add(gridDetails.getArenaCell(xPos, yPos-2))
                if (xPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos - 2))
                } else if (xPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos -2))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos -2))
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos -2))
                }
            }
            else if (yPos > 2) {
                cellArray.add(gridDetails.getArenaCell(xPos , yPos-2))
                cellArray.add(gridDetails.getArenaCell(xPos, yPos-3))
                if (xPos < 19) {
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos -2))
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos -3))
                }
                if (xPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos - 2))
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos - 3))
                }
            }

        }
        else {
            if (yPos == 17){
                cellArray.add(gridDetails.getArenaCell(xPos, yPos+2))
                if (xPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos + 2))
                } else if (xPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos +2))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos +2))
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos +2))
                }
            }
            else if (yPos < 17) {
                cellArray.add(gridDetails.getArenaCell(xPos , yPos+2))
                cellArray.add(gridDetails.getArenaCell(xPos, yPos+3))
                if (xPos < 19) {
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos +2))
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos +3))
                }
                if (xPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos + 2))
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos + 3))
                }
            }
        }

        for (item in cellArray){
            if (item.isObstacle()){
                return true
            }
        }
        return false
    }

    private fun checkIfCloseLeft(obstacle: ArenaCell): Boolean{

        var xPos = obstacle.getIndexColumn()
        var yPos = obstacle.getIndexRow()
        var direction = obstacle.getImageDirection()

        var cellArray: ArrayList<ArenaCell> = ArrayList<ArenaCell>()

        if (direction == Direction.NORTH){
            if (xPos == 17){
                cellArray.add(gridDetails.getArenaCell(xPos+2, yPos))
                if (yPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos - 1))
                } else if (yPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos + 1))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos + 1))
                }
            }
            else if (xPos < 17) {
                cellArray.add(gridDetails.getArenaCell(xPos + 2, yPos))
                cellArray.add(gridDetails.getArenaCell(xPos + 3, yPos))
                if (yPos < 19){
                    cellArray.add(gridDetails.getArenaCell(xPos+2, yPos + 1))
                    cellArray.add(gridDetails.getArenaCell(xPos+3, yPos + 1))

                    if (yPos > 0){
                        cellArray.add(gridDetails.getArenaCell(xPos+2, yPos - 1))
                        cellArray.add(gridDetails.getArenaCell(xPos+3, yPos - 1))
                    }
                }}
        }
        else if (direction == Direction.SOUTH){
            if (xPos == 2){
                cellArray.add(gridDetails.getArenaCell(xPos-2, yPos))
                if (yPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos - 1))
                } else if (yPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos + 1))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos + 1))
                }
            }
            else if (xPos > 2) {
                cellArray.add(gridDetails.getArenaCell(xPos - 2, yPos))
                cellArray.add(gridDetails.getArenaCell(xPos - 3, yPos))
                if (yPos < 19) {
                    cellArray.add(gridDetails.getArenaCell(xPos - 2, yPos + 1))
                    cellArray.add(gridDetails.getArenaCell(xPos - 3, yPos + 1))
                }
                if (yPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-2, yPos - 1))
                    cellArray.add(gridDetails.getArenaCell(xPos-3, yPos - 1))
                }
            }
        }
        else if (direction == Direction.EAST){
            if (yPos == 2){
                cellArray.add(gridDetails.getArenaCell(xPos, yPos-2))
                if (xPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos - 2))
                } else if (xPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos -2))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos -2))
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos -2))
                }
            }
            else if (yPos > 2) {
                cellArray.add(gridDetails.getArenaCell(xPos , yPos-2))
                cellArray.add(gridDetails.getArenaCell(xPos, yPos-3))
                if (xPos < 19) {
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos -2))
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos -3))
                }
                if (xPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos - 2))
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos - 3))
                }
            }

        }
        else {
            if (yPos == 17){
                cellArray.add(gridDetails.getArenaCell(xPos, yPos+2))
                if (xPos == 19){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos + 2))
                } else if (xPos == 0){
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos +2))
                } else {
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos +2))
                    cellArray.add(gridDetails.getArenaCell(xPos+1, yPos +2))
                }
            }
            else if (yPos < 17) {
                cellArray.add(gridDetails.getArenaCell(xPos , yPos+2))
                cellArray.add(gridDetails.getArenaCell(xPos, yPos+3))
                if (xPos < 19) {
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos +2))
                    cellArray.add(gridDetails.getArenaCell(xPos +1, yPos +3))
                }
                if (xPos > 0){
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos + 2))
                    cellArray.add(gridDetails.getArenaCell(xPos-1, yPos + 3))
                }
            }
        }

        for (item in cellArray){
            if (item.isObstacle()){
                return true
            }
        }
        return false

    }

    private fun turnAndTakePicture(queue: Queue<ArenaCell>){

        var cell = queue.remove()
        Log.d("ELEMENT", "Taking picture of obstacle at ${cell.getIndexColumn()}, ${cell.getIndexRow()}")

        var leftRight: String

        var obstacle = viewModel.arrayOfGridPoints[obstacleIndex]

        if (obstacle.xPos == 0 && viewModel.robotDirection.value == "NORTH" ||
            obstacle.yPos == 19 && viewModel.robotDirection.value == "EAST"||
            obstacle.yPos == 0 && viewModel.robotDirection.value == "WEST" ||
            obstacle.xPos == 19 && viewModel.robotDirection.value == "SOUTH"){
            leftRight = "L"
        }
        else {
            var closeLeft = checkIfCloseLeft(gridDetails.getArenaCell(obstacle.xPos, obstacle.yPos))
            var closeRight = checkIfCloseRight(gridDetails.getArenaCell(obstacle.xPos, obstacle.yPos))

            leftRight = if (closeLeft && !closeRight){
                "R"
            } else if (closeRight && !closeLeft){
                "L"
            } else {
                "M"
            }
        }

        Log.d("ELEMENT", "Robot at " +
                "${viewModel.robotPosition.value?.first()}, " +
                "${viewModel.robotPosition.value?.last()}, " +
                "FACING ${viewModel.robotDirection.value} NOW")

        //taking picture of obstacle in queue
        addTakePicCommand(cell, obstacleIndex, leftRight)

        if (!realRun) {
            val id = gridMap.mapDetailsIDs[cell.getIndexRow()][cell.getIndexColumn()]
            activity?.findViewById<TextView>(id)?.setBackgroundColor(Color.DKGRAY)
        }
    }

    // Takes in the fastest path and order of obstacles to visit from algo
    // Uses both above to show path on GridMap and to send commands to RPI
    private fun convertToCommands(path: Stack<ArenaCell>, orderedObstacles: Queue<ArenaCell>) {

        if (path.isEmpty()){
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("No Path Found!")
                .setMessage("No path was found for the current map config. The obstacles chosen are not reachable, please try a new map config instead.")
                .setPositiveButton("Okay") { _, _ ->

                }
                .show()
            return
        }

        // Pop the starting position off stack first
        path.pop()

        // Create runnable per cell movement that is delayed by 150ms
        val runnable: Runnable = object : Runnable {
            override fun run() {
                val robotPositionX = viewModel.robotPosition.value?.first()
                val robotPositionY = viewModel.robotPosition.value?.last()

                var element = path.pop()
                Log.d("ELEMENT", "${element.getIndexColumn()} ${element.getIndexRow()}, ${element.getRobotDirection()}")

                // check if it is the starting position of robot
                if (robotPositionY == element.getIndexRow() && robotPositionX == element.getIndexColumn() && viewModel.robotDirection.value == element.getRobotDirection().toString()) {

                    Log.v("ELEMENT", "time to take pic")
                    // pop the next element
                    turnAndTakePicture(orderedObstacles)
                    obstacleIndex += 1
                    //element = path.pop()
                }

                else if (robotPositionY == element.getIndexRow() && robotPositionX == element.getIndexColumn() && viewModel.robotDirection.value != element.getRobotDirection().toString()){
                    Log.v("ELEMENT", "rotating on the spot")
                    when (viewModel.robotDirection.value) {
                        getString(R.string.robot_direction_north) -> {
                            if (element.getRobotDirection() == Direction.WEST){
                                gridMap.onTheSpotLeft()
                                addRotateLeftCommand()
                            } else if (element.getRobotDirection() == Direction.EAST){
                                gridMap.onTheSpotRight()
                                addRotateRightCommand()
                            }
                        }
                        getString(R.string.robot_direction_south) -> {
                            if (element.getRobotDirection() == Direction.EAST){
                                gridMap.onTheSpotLeft()
                                addRotateLeftCommand()
                            } else if (element.getRobotDirection() == Direction.WEST){
                                gridMap.onTheSpotRight()
                                addRotateRightCommand()
                            }
                        }
                        getString(R.string.robot_direction_west) -> {
                            if (element.getRobotDirection() == Direction.SOUTH){
                                gridMap.onTheSpotLeft()
                                addRotateLeftCommand()
                            } else if (element.getRobotDirection() == Direction.NORTH){
                                gridMap.onTheSpotRight()
                                addRotateRightCommand()
                            }
                        }
                        getString(R.string.robot_direction_east) -> {
                            if (element.getRobotDirection() == Direction.NORTH){
                                gridMap.onTheSpotLeft()
                                addRotateLeftCommand()
                            } else if (element.getRobotDirection() == Direction.SOUTH){
                                gridMap.onTheSpotRight()
                                addRotateRightCommand()
                            }
                        }

                    }
                }

                // check robot position with popped element
                // if row is the same, column is different
                else if (robotPositionY == element.getIndexRow()) {
                    if (element.getIndexColumn() < robotPositionX!!) { // column is lesser than robot position
                        // check robot direction and decide action
                        when (viewModel.robotDirection.value) {
                            getString(R.string.robot_direction_east) -> {
                                gridMap.goDown()
                                addDownCommand()
                            }
                            getString(R.string.robot_direction_west) -> {
                                gridMap.goUp()
                                addUpCommand()
                            }
                        }
                    } // column is more than robot position
                    else {
                        when (viewModel.robotDirection.value) {
                            getString(R.string.robot_direction_east) -> {
                                gridMap.goUp()
                                addUpCommand()
                            }
                            getString(R.string.robot_direction_west) -> {
                                gridMap.goDown()
                                addDownCommand()
                            }
                        }
                    }
                }
                // if column is the same, row is different
                else if (robotPositionX == element.getIndexColumn()) {
                    if (element.getIndexRow() > robotPositionY!!) { // row is more than robot position
                        when (viewModel.robotDirection.value) {
                            getString(R.string.robot_direction_north) -> {
                                gridMap.goUp()
                                addUpCommand()
                            }
                            getString(R.string.robot_direction_south) -> {
                                gridMap.goDown()
                                addDownCommand()
                            }
                        }
                    }
                    else { // row is lesser than robot position
                        when (viewModel.robotDirection.value) {
                            getString(R.string.robot_direction_north) -> {
                                gridMap.goDown()
                                addDownCommand()
                            }
                            getString(R.string.robot_direction_south) -> {
                                gridMap.goUp()
                                addUpCommand()
                            }
                        }
                    }
                }
                // next path view is at the upper left
                else if (robotPositionX!! > element.getIndexColumn() && robotPositionY!! < element.getIndexRow()) {
                    when (viewModel.robotDirection.value) {
                        getString(R.string.robot_direction_north) -> {
                            gridMap.goForwardLeft()
                            addLeftForwardCommand()
                        }
                        getString(R.string.robot_direction_east) -> {
                            gridMap.goBackLeft()
                            addLeftBackCommand()
                        }
                        getString(R.string.robot_direction_south) -> {
                            gridMap.goBackRight()
                            addRightBackCommand()
                        }
                        getString(R.string.robot_direction_west) -> {
                            gridMap.goForwardRight()
                            addRightForwardCommand()
                        }
                    }
                }
                // next path view is at the upper right
                else if (robotPositionX < element.getIndexColumn() && robotPositionY!! < element.getIndexRow()) {
                    when (viewModel.robotDirection.value) {
                        getString(R.string.robot_direction_north) -> {
                            gridMap.goForwardRight()
                            addRightForwardCommand()
                        }
                        getString(R.string.robot_direction_east) -> {
                            gridMap.goForwardLeft()
                            addLeftForwardCommand()
                        }
                        getString(R.string.robot_direction_south) -> {
                            gridMap.goBackLeft()
                            addLeftBackCommand()
                        }
                        getString(R.string.robot_direction_west) -> {
                            gridMap.goBackRight()
                            addRightBackCommand()
                        }
                    }
                }
                // next path view is at the bottom right
                else if (robotPositionX < element.getIndexColumn() && robotPositionY!! > element.getIndexRow()) {
                    when (viewModel.robotDirection.value) {
                        getString(R.string.robot_direction_north) -> {
                            gridMap.goBackRight()
                            addRightBackCommand()
                        }
                        getString(R.string.robot_direction_east) -> {
                            gridMap.goForwardRight()
                            addRightForwardCommand()
                        }
                        getString(R.string.robot_direction_south) -> {
                            gridMap.goForwardLeft()
                            addLeftForwardCommand()
                        }
                        getString(R.string.robot_direction_west) -> {
                            gridMap.goBackLeft()
                            addLeftBackCommand()
                        }
                    }
                }
                // next path view is at the bottom left
                else if (robotPositionX > element.getIndexColumn() && robotPositionY!! > element.getIndexRow()) {
                    when (viewModel.robotDirection.value) {
                        getString(R.string.robot_direction_north) -> {
                            gridMap.goBackLeft()
                            addLeftBackCommand()
                        }
                        getString(R.string.robot_direction_east) -> {
                            gridMap.goBackRight()
                            addRightBackCommand()
                        }
                        getString(R.string.robot_direction_south) -> {
                            gridMap.goForwardRight()
                            addRightForwardCommand()
                        }
                        getString(R.string.robot_direction_west) -> {
                            gridMap.goForwardLeft()
                            addLeftForwardCommand()
                        }
                    }
                }
                if (!path.isEmpty()) { // Proceed to next cell
                    // just a delay
                    handler.postDelayed(this, 50)
                }
                else{ // Last obstacle found
                    turnAndTakePicture(orderedObstacles) //At last obstacle to take picture
                    obstacleIndex += 1
                    Log.d(TAG, "Size of commands queue is" + commandsQueue.size.toString())
                    commandsQueue.add("ggwp")
                    executeCommands()
                    obstacleIndex = 0
                }
            }
        }

        handler.post(runnable)
    }

    private fun addLeftForwardCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = upLeftCmd, distance = null, angle = "90", index = null, leftRight = null))
    }

    private fun addRightForwardCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = upRightCmd, distance = null, angle = "90", index = null, leftRight = null))
    }

    private fun addUpCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = upCmd, distance = DEFAULT_DISTANCE.toString(), angle = null, index = null, leftRight = null))
    }

    private fun addDownCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = downCmd, distance = DEFAULT_DISTANCE.toString(), angle = null, index = null, leftRight = null))
    }

    private fun addLeftBackCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = downLeftCmd, distance = null, angle = "90", index = null, leftRight = null))
    }

    private fun addRightBackCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = downRightCmd, distance = null, angle = "90", index = null, leftRight = null))
    }

    private fun addRotateLeftCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = onTheSpotLeft, distance = null, angle = "90", index = null, leftRight = null))
    }

    private fun addRotateRightCommand() {
        commandsQueue.add(CommandGenerator.generateCommand(direction = onTheSpotRight, distance = null, angle = "90", index = null, leftRight = null))
    }

    private fun addTakePicCommand(cell: ArenaCell, obstacleIndex: Int, leftRight: String) {
        commandsQueue.add(CommandGenerator.generateCommand(direction = takePic, distance = null, angle = null, index = obstacleIndex.toString(), leftRight = leftRight))
    }

    private fun executeCommands() {

        var stringToSend = ""
        var tempForwardCounter = 0
        var tempBackwardCounter = 0

        while (commandsQueue.isNotEmpty()){
            var cmd = commandsQueue.remove()

            Log.d(TAG, cmd)

            if (cmd == "w10XX|" && commandsQueue.size == 1){
                stringToSend += "${cmd},"
                tempForwardCounter = 0
            }
            if (cmd == "x10XX|" && commandsQueue.size == 1){
                stringToSend += "${cmd},"
                tempBackwardCounter = 0
            }
            else if (cmd == "w10XX|"){
                tempForwardCounter++
            }
            else if (cmd == "x10XX|"){
                tempBackwardCounter++
            }
            else if ((cmd != "w10XX|" && tempForwardCounter == 0) && (cmd != "x10XX|" && tempBackwardCounter == 0)) { // send the command as is
                stringToSend += "${cmd},"
            }
            else if ((cmd != "w10XX|" && tempForwardCounter != 0) || (cmd != "x10XX|" && tempBackwardCounter != 0)) { //add all the w commands or z commands
                var distanceString = ""
                var combinedCommand = ""

                if (tempForwardCounter != 0) {
                    distanceString = tempForwardCounter.toString() + "0"
                    combinedCommand = CommandGenerator.generateCommand(direction = upCmd, distance = distanceString, angle = null, index = null, leftRight = null)
                    tempForwardCounter = 0
                }
                else if (tempBackwardCounter != 0) {
                    distanceString = tempBackwardCounter.toString() + "0"
                    combinedCommand = CommandGenerator.generateCommand(direction = downCmd, distance = distanceString, angle = null, index = null, leftRight = null)
                    tempBackwardCounter = 0
                }

                stringToSend += "${combinedCommand},"

                stringToSend += "${cmd}," // send the command that is not w
            }
        }

        Log.d("COMMAND", stringToSend)
        mbluetoothService.write((stringToSend).toByteArray())
        //stringToSend = ""
    }

}