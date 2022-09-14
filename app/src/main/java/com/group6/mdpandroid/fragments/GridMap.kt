package com.group6.mdpandroid.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.group6.mdpandroid.R
import com.group6.mdpandroid.entity.GridPoint
import com.group6.mdpandroid.utils.Constants
import com.group6.mdpandroid.viewmodels.RobotMapViewModel
import com.group6Android.mdp.bluetooth.BluetoothService

class GridMap : Fragment(), View.OnDragListener, View.OnLongClickListener, View.OnClickListener {

    private val TAG = "GridMap"

    private val mRows = 21
    private val mCols = 21
    private val viewModel: RobotMapViewModel by activityViewModels()
    var mapDetailsIDs = Array(20) { Array(20) { -1 } }
    private var mapDetails: MutableMap<Int, Char> = mutableMapOf()
    private var robotPosition = arrayOf(0, 0)
    private var obstacleCount = 0
    var usedCells = listOf(0)

    /**
     * Member object for the bluetooth service
     */
    private lateinit var mbluetoothService: BluetoothService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::mbluetoothService.isInitialized) {
//            mbluetoothService = (activity as MainActivity).mBluetoothService
            Log.d(TAG, "mBluetoothService not initalized in GridMap")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_grid_map, container, false)
        val layout = root.findViewById<ConstraintLayout>(R.id.layout)
        var id: Int
        var textView: TextView
        var lp: ConstraintLayout.LayoutParams
        val cs = ConstraintSet()
        val idArray = Array(mRows) { IntArray(mCols) }
        val clearButton = activity?.findViewById<Button>(R.id.clear_button)
        clearButton?.setOnClickListener(){
            resetMap()
            Log.d(TAG, "Clear Button pressed")
        }


        // Add our views to the ConstraintLayout.
        for (iRow in 0 until mRows) {
            for (iCol in 0 until mCols) {

                // Create new TextView
                textView = TextView(context)
                textView.setOnDragListener(this)

                // Set constraints
                lp = ConstraintLayout.LayoutParams(
                    ConstraintSet.MATCH_CONSTRAINT,
                    ConstraintSet.MATCH_CONSTRAINT
                )

                // Generate ID and tag for TextView
                id = View.generateViewId()
                lp.setMargins(1, 1, 1, 1)
                idArray[iRow][iCol] = id
                textView.id = id
                val gridPoint = GridPoint(
                    Constants.Companion.GridPointType.EMPTY,
                    iCol - 1,
                    iRow - 1,
                    null
                )
                textView.tag = gridPoint

                // Add ID to mapDetailsIDs if in map
                // Map Id as Key to Char based on status value to mapDetails
                if (iRow != 0 && iCol != 0) {
                    mapDetailsIDs[iRow - 1][iCol - 1] = id
                    checkAndUpdateMap(textView)
                }

                // For (0,0) TextView which holds nothing
                if (iRow == 0 && iCol == 0) textView.visibility = View.INVISIBLE

                // For left-most column and bottom-most row
                else if (iRow == 0 || iCol == 0) {
                    textView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    textView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                    textView.gravity = Gravity.CENTER
                    textView.setOnClickListener(null)
                    textView.setOnDragListener(null)
                    gridPoint.value = Constants.Companion.GridPointType.EDGE

                    if (iRow == 0) textView.text = (iCol - 1).toString()
                    else if (iCol == 0) textView.text = (iRow - 1).toString()
                }

                // For grid map itself
                else {
                    textView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_purple
                        )
                    )
                    textView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
                layout.addView(textView, lp)
            }
        }

        //printMap() // To check if map has been initialised successfully

        // Create horizontal chain for each row and set the 1:1 dimensions.
        // but first make sure the layout frame has the right ratio set.
        cs.clone(layout)
        cs.setDimensionRatio(R.id.gridView, "$mCols:$mRows")
        for (iRow in 0 until mRows) {
            for (iCol in 0 until mCols) {
                id = idArray[iRow][iCol]
                cs.setDimensionRatio(id, "1:1")
                if (iRow == 0) {
                    cs.connect(id, ConstraintSet.BOTTOM, R.id.gridView, ConstraintSet.BOTTOM)
                } else {
                    cs.connect(id, ConstraintSet.BOTTOM, idArray[iRow - 1][0], ConstraintSet.TOP)
                }
            }
            // Create a horizontal chain that will determine the dimensions of our squares.
            // Could also be createHorizontalChainRtl() with START/END.
            cs.createHorizontalChain(
                R.id.gridView, ConstraintSet.LEFT,
                R.id.gridView, ConstraintSet.RIGHT,
                idArray[iRow], null, ConstraintSet.CHAIN_PACKED
            )
        }

        cs.applyTo(layout)
        return root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.robotPosition.observe(viewLifecycleOwner) { posArray ->
            robotPosition = posArray
        }

        viewModel.leftForwardToggle.observe(viewLifecycleOwner) {
            goForwardLeft()
        }

        viewModel.rightForwardToggle.observe(viewLifecycleOwner) {
            goForwardRight()
        }

        viewModel.upToggle.observe(viewLifecycleOwner) {
            goUp()
        }

        viewModel.downToggle.observe(viewLifecycleOwner) {
            goDown()
        }
        viewModel.leftBackToggle.observe(viewLifecycleOwner) {
            goBackLeft()
        }

        viewModel.rightBackToggle.observe(viewLifecycleOwner) {
            goBackRight()
        }

        viewModel.onTheSpotLeftToggle.observe(viewLifecycleOwner) {
            onTheSpotLeft()
        }

        viewModel.onTheSpotRightToggle.observe(viewLifecycleOwner) {
            onTheSpotRight()
        }
    }

    override fun onDrag(v: View?, e: DragEvent?): Boolean {

        var imgBtn: ImageButton? = null
        var textView: TextView? = null
        var color = R.color.light_purple
        v as TextView

        if (e?.localState is AppCompatImageButton) {
            imgBtn = e.localState as ImageButton

            color = when (imgBtn.id) {
                R.id.obstacleControl -> R.color.teal_700
                R.id.robotControl -> R.color.teal_200
                else -> R.color.light_purple
            }
        } else if (e?.localState is TextView) {
            textView = e.localState as TextView
            val dragData = textView.tag as GridPoint
            color = when (dragData.value) {
                Constants.Companion.GridPointType.OBSTACLE_TOP -> R.color.teal_700
                Constants.Companion.GridPointType.ROBOT_CENTER -> R.color.teal_200
                else -> R.color.light_purple
            }
        }

        when (e?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                e.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                // If enter hovering over a robot/obstacle, do not change colour
                if ((v.tag as GridPoint).value == Constants.Companion.GridPointType.EMPTY) {
                    v.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
                    v.invalidate()
                }
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                if ((v.tag as GridPoint).value == Constants.Companion.GridPointType.EMPTY) {
                    v.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
                    activity?.findViewById<TextView>(R.id.current_grid_x_y)?.text =
                        "${(v.tag as GridPoint).xPos.toString()}, ${(v.tag as GridPoint).yPos.toString()}"
                    v.invalidate()
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                // If exit hovering over an empty grid, need to recolour it back again
                if ((v.tag as GridPoint).value == Constants.Companion.GridPointType.EMPTY) {
                    v.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_purple
                        )
                    )
                }
                v.invalidate()
                v.setOnDragListener(this)
                true
            }
            // IF ITEM IS DROPPED ON A GRID TEXTVIEW
            DragEvent.ACTION_DROP -> {

                val gridData = v.tag as GridPoint
                var result = false


                // ===== Check for clashes first! =====//
                when {

                    imgBtn == null -> {
                        if (color == R.color.teal_200) {
                            Log.v("SR", "Checking if invalid placement for existing robot!")
                            result = checkIfInvalidPlacement(v.id, true)
                        } else if (color == R.color.teal_700) {
                            Log.v("SR", "Checking if invalid placement for existing obstacle!")
                            result = checkIfInvalidPlacement(v.id, false)
                        }
                    }

                    // For adding of new obstacle
                    imgBtn.id == R.id.obstacleControl -> {
                        Log.v("SR", "Checking if invalid placement for new obstacle!")
                        result = checkIfInvalidPlacement(v.id, false)
                    }

                    // For adding of new robot
                    imgBtn.id == R.id.robotControl -> {
                        Log.v("SR", "Checking if invalid placement for new robot!")
                        result = checkIfInvalidPlacement(v.id, true)
                    }
                }

                //===== If no clashes detected, proceed to draw =====//
                if (result) {

                    // For moving existing robot OR obstacle, set previous TextView data
                    if (textView != null) {

                        val dragData = textView.tag as GridPoint

                        // Transfer data to new TextView
                        gridData.value = dragData.value
                        gridData.textInside = dragData.textInside

                        // Reset data for old TextView
                        textView.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.light_purple
                            )
                        )
                        dragData.value = Constants.Companion.GridPointType.EMPTY
                    }

                    when {
                        // For moving existing robot OR obstacle from previous location
                        // Also remove onClick listener and text at previous location
                        imgBtn == null -> {
                            if (color == R.color.teal_200) {
                                Log.v("SR", "Moving robot!")
                                drawRobot(v)
                            } else if (color == R.color.teal_700) {
                                Log.v("SR", "Moving obstacle!")
                                drawObstacle(v, R.drawable.north_gradient_background)
                            }
                            textView?.text = ""
                        }

                        // For adding of new obstacle
                        imgBtn.id == R.id.obstacleControl -> {
                            Log.v("SR", "Adding new obstacle!")
                            gridData.value = Constants.Companion.GridPointType.OBSTACLE_TOP
                            gridData.textInside = obstacleCount
                            drawObstacle(v, R.drawable.north_gradient_background)
                            obstacleCount++
                            activity?.findViewById<TextView>(R.id.num_of_obstacles)?.text = (gridData.textInside!! + 1).toString()
                        }

                        // For adding of new robot
                        imgBtn.id == R.id.robotControl -> {
                            Log.v("SR", "Adding new robot!")
                            gridData.value = Constants.Companion.GridPointType.ROBOT_CENTER
                            drawRobot(v)
                            viewModel.setRobotDirection(getString(R.string.robot_direction_north))
                        }
                    }

                    // Update view model on robot's position if needed
                    when (e.clipData?.getItemAt(0)?.text) {
                        getString(R.string.type_robot),
                        Constants.Companion.GridPointType.ROBOT_CENTER.ordinal.toString() -> {
                            val posArray = arrayOf(gridData.xPos, gridData.yPos)
                            viewModel.setRobotPosition(posArray)
                            imgBtn?.isEnabled = false
                        }
                    }

                    // Update view model on obstacle's position if needed
                    when (e.clipData?.getItemAt(0)?.text) {
                        getString(R.string.type_obstacle),
                        Constants.Companion.GridPointType.OBSTACLE.ordinal.toString(),
                        Constants.Companion.GridPointType.OBSTACLE_TOP.ordinal.toString(),
                        Constants.Companion.GridPointType.OBSTACLE_BOTTOM.ordinal.toString(),
                        Constants.Companion.GridPointType.OBSTACLE_LEFT.ordinal.toString(),
                        Constants.Companion.GridPointType.OBSTACLE_RIGHT.ordinal.toString(),
                        -> {
                            viewModel.addGridPointToArray(gridData)
                        }
                    }

                    v.setOnLongClickListener(this)
                    v.invalidate()
                }

                //===== Clashes detected, don't allow addition/movement of robot/obstacle =====//
                else {
                    // If new textview is not empty, don't need to do anything
                    // If new textview is previously EMPTY, recolour it to be empty
                    if (gridData.value == Constants.Companion.GridPointType.EMPTY) {
                        v.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.light_purple
                            )
                        )
                    }

                    // If it was for moving of existing robot, redraw edges of robot
                    if (imgBtn?.id == R.id.robotControl || color == R.color.teal_200) {
                        if (textView != null) {
                            drawRobot(textView)
                        }
                    }
                }
            }

            // IF ITEM IS NOT DROPPED ON A GRID TEXTVIEW
            DragEvent.ACTION_DRAG_ENDED -> {

                // For removing of obstacle or robot
                if (!e.result && imgBtn == null) {
                    val dragData = textView?.tag as GridPoint

                    // If robot removed, re-enable button to add robot
                    // Set robot position to (-1,-1) - not in grid
                    if (dragData.value == Constants.Companion.GridPointType.ROBOT_CENTER) {
                        imgBtn = activity?.findViewById(R.id.robotControl)!!
                        imgBtn.isEnabled = true
                        val posArray = arrayOf(-1, -1)
                        viewModel.setRobotPosition(posArray)
                        viewModel.addStatusText("SUB, ROBOT")
                    } else {
                        activity?.findViewById<TextView>(R.id.num_of_obstacles)?.text = (dragData.textInside!!).toString()
                    }

                    //Change colour back to purple and set grid type to EMPTY and textview text to empty
                    textView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_purple
                        )
                    )
                    dragData.value = Constants.Companion.GridPointType.EMPTY
                    textView.text = ""
                }
            }

        }
        return true
    }

    // Called when a grid point is long-clicked, to move/remove obstacle/robot
    override fun onLongClick(v: View?): Boolean {
        clearItem(v!!)
        return true
    }

    // Called when a grid point is clicked, to rotate obstacle/robot
    override fun onClick(v: View?) {
        debugCurrentItem(v!!)
        rotateItem(v)
    }

    private fun checkIfInvalidPlacement(centerPoint: Int, isRobot: Boolean): Boolean {
        Log.v("SR", "checking for invalid placement!")
        var gridData = GridPoint()
        val points: Array<Int>

        if (isRobot) {
            // Retrieving IDs of robot edges
            val topLeft = centerPoint + 21 - 1
            val topCenter = topLeft + 1
            val topRight = topCenter + 1
            val centerLeft = centerPoint - 1
            val centerRight = centerPoint + 1
            val bottomLeft = centerPoint - 21 - 1
            val bottomCenter = bottomLeft + 1
            val bottomRight = bottomCenter + 1

            // Need to check all 9 text views for new placement of robot
            points = arrayOf(
                topLeft, topCenter, topRight,
                centerLeft, centerPoint, centerRight,
                bottomLeft, bottomCenter, bottomRight
            )
        } else {
            // Only has one textview to check which is the new placement for obstacles
                points = arrayOf(centerPoint)
        }

        for (target in points) {
            val textView = activity?.findViewById<View>(target) ?: return false
            if (textView.tag != null) gridData = textView.tag as GridPoint

            // As long as new grid points are not empty or previously robot, clash will occur
            if (gridData.value !== Constants.Companion.GridPointType.EMPTY &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_BOTTOM &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_CENTER &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_TOP &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_LEFT &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_OTHER &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_RIGHT &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_ONTHESPOTLEFT &&
                gridData.value !== Constants.Companion.GridPointType.ROBOT_ONTHESPOTRIGHT
            ) {
                return false
            }
        }
        return true
    }

    // -----Called to update the grid points around the robot's center----- //
    // Used in onLongClick(): When a robot is long-clicked on for moving/removal
    // Used after DragEvent.ACTION_DROP: When a robot is placed on the map
    private fun setRobotOtherGrids(centerPoint: Int, color: Int, clear: Boolean) {
        val topLeft = centerPoint + 21 - 1
        val topCenter = topLeft + 1
        val topRight = topCenter + 1
        val centerLeft = centerPoint - 1
        val centerRight = centerPoint + 1
        val bottomLeft = centerPoint - 21 - 1
        val bottomCenter = bottomLeft + 1
        val bottomRight = bottomCenter + 1

        var gridData = GridPoint()
        var direction = Constants.Companion.GridPointType.EMPTY

        val points = arrayOf(
            topLeft, topCenter, topRight,
            centerLeft, centerRight,
            bottomLeft, bottomCenter, bottomRight
        )

        for (target in points) {
            val textView = activity?.findViewById<View>(target)
            if (textView?.tag != null) gridData = textView.tag as GridPoint
            if (gridData.value != Constants.Companion.GridPointType.EDGE) {
                gridData.value = when (textView) {
                    activity?.findViewById<TextView>(topCenter)
                    -> Constants.Companion.GridPointType.ROBOT_TOP
                    activity?.findViewById<TextView>(bottomCenter)
                    -> Constants.Companion.GridPointType.ROBOT_BOTTOM
                    activity?.findViewById<TextView>(centerLeft)
                    -> Constants.Companion.GridPointType.ROBOT_LEFT
                    activity?.findViewById<TextView>(centerRight)
                    -> Constants.Companion.GridPointType.ROBOT_RIGHT
                    else -> Constants.Companion.GridPointType.ROBOT_OTHER
                }

                // check current direction of robot and color correct textview
                direction = when (viewModel.robotDirection.value.toString()) {
                    getString(R.string.robot_direction_east) -> Constants.Companion.GridPointType.ROBOT_RIGHT
                    getString(R.string.robot_direction_south) -> Constants.Companion.GridPointType.ROBOT_BOTTOM
                    getString(R.string.robot_direction_west) -> Constants.Companion.GridPointType.ROBOT_LEFT
                    else -> Constants.Companion.GridPointType.ROBOT_TOP
                }

                if (gridData.value == direction)
                    textView?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
            }

            if (!clear) {
                if (gridData.value != direction)
                    textView?.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
            } else {
                textView?.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
                gridData.value = Constants.Companion.GridPointType.EMPTY
                textView?.setOnDragListener(this)
            }
        }
    }

    // ------Robot movement functions------ //`
    private fun robotCurrentDirectionMovement(direction: String, directionType: Int) {
        var robotViewID = mapDetailsIDs[viewModel.robotPosition.value?.get(1)!!][viewModel.robotPosition.value?.get(0)!!]
        val oldCenter = activity?.findViewById<TextView>(robotViewID)

        // updated view on movement and rotation
        var newView = TextView(context) // for direction view
        var newRobotCenter = TextView(context) // for robot center

        // clear old center before update
        clearItem(oldCenter!!)

        when (direction) {
            getString(R.string.robot_direction_north) -> {
                // up
                //main path checked
                when (directionType) {
                    1 -> {
                        newView = activity?.findViewById(robotViewID + 21)!!
                        highlightPath(arrayOf(robotViewID - 22, robotViewID - 21, robotViewID - 20))
                        highlightMainPath(arrayOf(robotViewID+42, robotViewID, robotViewID+21))
                    }

                    // forward left
                    // main path checked
                    2 -> {
                        newView = activity?.findViewById(robotViewID + 38)!!
                        newRobotCenter = activity?.findViewById(robotViewID + 39)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID - 22, robotViewID - 21, robotViewID - 20,
                            robotViewID - 1, robotViewID + 1, robotViewID + 20,
                            robotViewID + 21, robotViewID + 22, robotViewID + 41,
                            robotViewID + 42, robotViewID + 43, robotViewID + 62,
                            robotViewID + 63, robotViewID + 64
                        ))
                        highlightMainPath(arrayOf(robotViewID-21,robotViewID +21, robotViewID+42, robotViewID+41, robotViewID+40, robotViewID+39, robotViewID))
                    }

                    // forward right
                    // main path checked
                    3 -> {
                        newView = activity?.findViewById(robotViewID + 46)!!
                        newRobotCenter = activity?.findViewById(robotViewID + 45)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID - 22, robotViewID - 21, robotViewID - 20,
                            robotViewID - 1, robotViewID + 1, robotViewID + 20,
                            robotViewID + 21, robotViewID + 22, robotViewID + 41,
                            robotViewID + 42, robotViewID + 43, robotViewID + 62,
                            robotViewID + 63, robotViewID + 64
                        ))
                        highlightMainPath(arrayOf(robotViewID-21, robotViewID +21, robotViewID+42, robotViewID+43,robotViewID+44, robotViewID+45, robotViewID))
                    }

                    //down
                    // main path checked
                    4 -> {
                        newView = activity?.findViewById(robotViewID - 21)!!
                        highlightPath(arrayOf(robotViewID + 20, robotViewID + 21, robotViewID + 22))
                        highlightMainPath(arrayOf(robotViewID, robotViewID-21))
                    }

                    // down left
                    // main path checked
                    5 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 86)!!
                        newView = activity?.findViewById(robotViewID - 85)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 22, robotViewID + 21, robotViewID + 1,
                            robotViewID - 20, robotViewID - 21, robotViewID - 41,
                            robotViewID - 42, robotViewID - 62, robotViewID - 63,
                            robotViewID - 83, robotViewID - 84, robotViewID - 104,
                            robotViewID - 105, robotViewID - 43, robotViewID - 22,
                            robotViewID - 1, robotViewID + 20
                        ))
                        highlightMainPath(arrayOf(robotViewID -21, robotViewID-42, robotViewID-63, robotViewID-84, robotViewID-85, robotViewID-86,robotViewID))
                    }

                    // down right
                    // main path checked
                    6 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 82)!!
                        newView = activity?.findViewById(robotViewID - 83)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 21, robotViewID + 20, robotViewID - 1,
                            robotViewID - 21, robotViewID - 22, robotViewID - 42,
                            robotViewID - 43, robotViewID - 63, robotViewID - 64,
                            robotViewID - 84, robotViewID - 85, robotViewID - 105,
                            robotViewID - 106, robotViewID - 41, robotViewID - 20,
                            robotViewID + 1, robotViewID + 22
                        ))
                        highlightMainPath(arrayOf(robotViewID -21, robotViewID-42, robotViewID-63, robotViewID-84,robotViewID-83, robotViewID-82, robotViewID))
                    }

                    //on the spot left
                    // main path checked
                    7 -> {
                        newView = activity?.findViewById(robotViewID - 1)!!
                        highlightMainPath(arrayOf(robotViewID))
                    }

                    //on the spot right
                    // main path checked
                    8 -> {
                        newView = activity?.findViewById(robotViewID + 1)!!
                        highlightMainPath(arrayOf(robotViewID))
                    }
                }
            }
            getString(R.string.robot_direction_east) -> {
                // up
                // main path checked
                if (directionType == 1) {
                    newView = activity?.findViewById(robotViewID + 1)!!
                    highlightPath(arrayOf(robotViewID + 20, robotViewID - 1, robotViewID - 22))
                    highlightMainPath(arrayOf(robotViewID + 1, robotViewID))
                }
                // forward left
                //main path checked
                when (directionType) {
                    2 -> {
                        newRobotCenter = activity?.findViewById(robotViewID + 65)!!
                        newView = activity?.findViewById(robotViewID + 86)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID - 1, robotViewID + 20, robotViewID - 22,
                            robotViewID - 21, robotViewID + 21, robotViewID - 20,
                            robotViewID + 1, robotViewID + 22, robotViewID - 19,
                            robotViewID + 2, robotViewID + 23, robotViewID - 18,
                            robotViewID + 3, robotViewID + 24
                        ))
                        highlightMainPath(arrayOf(robotViewID, robotViewID+1,robotViewID+2, robotViewID+23, robotViewID+44, robotViewID+65, robotViewID+86))
                    }

                    // forward right
                    //main path checked
                    3 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 61)!!
                        newView = activity?.findViewById(robotViewID - 82)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID - 1, robotViewID + 20, robotViewID - 22,
                            robotViewID - 21, robotViewID + 21, robotViewID - 20,
                            robotViewID + 1, robotViewID + 22, robotViewID - 19,
                            robotViewID + 2, robotViewID + 23, robotViewID - 18,
                            robotViewID + 3, robotViewID + 24
                        ))
                        highlightMainPath(arrayOf(robotViewID, robotViewID+1,robotViewID+2, robotViewID-19, robotViewID-40, robotViewID-61, robotViewID-82))
                    }

                    // down
                    // main path checked
                    4 -> {
                        newView = activity?.findViewById(robotViewID - 1)!!
                        highlightPath(arrayOf(robotViewID + 22, robotViewID + 1, robotViewID - 20))
                        highlightMainPath(arrayOf(robotViewID -1, robotViewID))
                    }

                    // down left
                    // main path checked
                    5 -> {
                        newRobotCenter = activity?.findViewById(robotViewID + 38)!!
                        newView = activity?.findViewById(robotViewID + 17)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID - 21, robotViewID - 1, robotViewID - 22,
                            robotViewID - 2, robotViewID - 23, robotViewID - 3,
                            robotViewID - 24, robotViewID - 4, robotViewID - 25,
                            robotViewID - 5, robotViewID - 26, robotViewID + 19,
                            robotViewID + 20, robotViewID + 21, robotViewID + 1,
                            robotViewID + 22, robotViewID - 20
                        ))
                        highlightMainPath(arrayOf(robotViewID, robotViewID-1,robotViewID-2, robotViewID-3, robotViewID-4,robotViewID+17,robotViewID+38))
                    }

                    // down right
                    // main path checked
                    6 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 46)!!
                        newView = activity?.findViewById(robotViewID - 25)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 21, robotViewID - 1, robotViewID + 20,
                            robotViewID - 2, robotViewID + 19, robotViewID - 3,
                            robotViewID + 18, robotViewID - 4, robotViewID + 17,
                            robotViewID - 5, robotViewID + 16, robotViewID + 1,
                            robotViewID + 22, robotViewID - 20, robotViewID - 21,
                            robotViewID - 22, robotViewID - 23
                        ))
                        highlightMainPath(arrayOf(robotViewID, robotViewID-1,robotViewID-2, robotViewID-3, robotViewID-4,robotViewID-25,robotViewID-46))
                    }

                    //on the spot left
                    // main path checked
                    7 -> {
                        newView = activity?.findViewById(robotViewID + 21)!!
                        highlightPath(arrayOf(robotViewID))
                    }

                    //on the spot right
                    //main path checked
                    8 -> {
                        newView = activity?.findViewById(robotViewID - 21)!!
                        highlightPath(arrayOf(robotViewID))
                    }
                }
            }
            getString(R.string.robot_direction_south) -> {
                // up
                // main path checked
                when (directionType) {
                    1 -> {
                        newView = activity?.findViewById(robotViewID - 21)!!
                        highlightPath(arrayOf(robotViewID + 20, robotViewID + 21, robotViewID + 22))
                        highlightMainPath(arrayOf(robotViewID -21, robotViewID))
                    }

                    // forward left
                    // main path checked
                    2 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 39)!!
                        newView = activity?.findViewById(robotViewID - 38)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 21, robotViewID + 20, robotViewID + 22,
                            robotViewID - 1, robotViewID + 1, robotViewID - 20,
                            robotViewID - 21, robotViewID - 22, robotViewID - 41,
                            robotViewID - 42, robotViewID - 43, robotViewID - 62,
                            robotViewID - 63, robotViewID - 64
                        ))
                        highlightMainPath(arrayOf(robotViewID -21, robotViewID-42,robotViewID-41, robotViewID-40, robotViewID-39, robotViewID-38, robotViewID))
                    }

                    // forward right
                    // main path checked
                    3 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 45)!!
                        newView = activity?.findViewById(robotViewID - 46)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 21, robotViewID + 20, robotViewID + 22,
                            robotViewID - 1, robotViewID + 1, robotViewID - 20,
                            robotViewID - 21, robotViewID - 22, robotViewID - 41,
                            robotViewID - 42, robotViewID - 43, robotViewID - 62,
                            robotViewID - 63, robotViewID - 64
                        ))
                        highlightMainPath(arrayOf(robotViewID -21, robotViewID-42,robotViewID-43, robotViewID-44, robotViewID-45, robotViewID-46, robotViewID))
                    }

                    // down
                    // main path checked
                    4 -> {
                        newView = activity?.findViewById(robotViewID + 21)!!
                        highlightPath(arrayOf(robotViewID - 20, robotViewID - 21, robotViewID - 22))
                        highlightMainPath(arrayOf(robotViewID + 21, robotViewID))
                    }

                    // down left
                    // main path checked
                    5 -> {
                        newRobotCenter = activity?.findViewById(robotViewID + 86)!!
                        newView = activity?.findViewById(robotViewID + 85)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID - 1, robotViewID + 21, robotViewID + 20,
                            robotViewID + 42, robotViewID + 41, robotViewID + 63,
                            robotViewID + 62, robotViewID + 84, robotViewID + 83,
                            robotViewID + 104, robotViewID + 105, robotViewID + 64,
                            robotViewID + 43, robotViewID + 22, robotViewID + 1,
                            robotViewID - 20, robotViewID - 21, robotViewID - 22
                        ))
                        highlightMainPath(arrayOf(robotViewID, robotViewID+21, robotViewID+42, robotViewID+63, robotViewID+84, robotViewID+85, robotViewID+86))
                    }

                    // down right
                    // main path checked
                    6 -> {
                        newRobotCenter = activity?.findViewById(robotViewID + 82)!!
                        newView = activity?.findViewById(robotViewID + 83)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 1, robotViewID + 21, robotViewID + 22,
                            robotViewID + 42, robotViewID + 43, robotViewID + 63,
                            robotViewID + 64, robotViewID + 84, robotViewID + 85,
                            robotViewID + 105, robotViewID + 106, robotViewID - 20,
                            robotViewID - 21, robotViewID - 22, robotViewID - 1,
                            robotViewID + 20, robotViewID + 41
                        ))
                        highlightMainPath(arrayOf( robotViewID-21, robotViewID, robotViewID+21, robotViewID+42, robotViewID+63, robotViewID+84, robotViewID+83, robotViewID+82))
                    }

                    //on the spot left
                    // main path checked
                    7 -> {
                        newView = activity?.findViewById(robotViewID + 1)!!
                        highlightMainPath(arrayOf(robotViewID))
                    }

                    //on the spot right
                    // main path checked
                    8 -> {
                        newView = activity?.findViewById(robotViewID - 1)!!
                        highlightMainPath(arrayOf(robotViewID))
                    }
                }
            }
            getString(R.string.robot_direction_west) -> {
                // up
                // main path checked
                when (directionType) {
                    1 -> {
                        newView = activity?.findViewById(robotViewID - 1)!!
                        highlightPath(arrayOf(robotViewID + 22, robotViewID + 1, robotViewID - 20))
                        highlightMainPath(arrayOf(robotViewID -1, robotViewID))
                    }

                    // forward left
                    // main path might need recheck for view
                    2 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 65)!!
                        newView = activity?.findViewById(robotViewID - 86)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 1, robotViewID + 22, robotViewID - 20,
                            robotViewID + 21, robotViewID - 21, robotViewID + 20,
                            robotViewID - 1, robotViewID - 22, robotViewID + 19,
                            robotViewID - 2, robotViewID - 23, robotViewID + 18,
                            robotViewID - 3, robotViewID - 24
                        ))
                        highlightMainPath(arrayOf(robotViewID -1, robotViewID-2, robotViewID-23, robotViewID-44, robotViewID-65,robotViewID, robotViewID-86))
                    }

                    //  forward right
                    // main path might need recheck for view
                    3 -> {
                        newRobotCenter = activity?.findViewById(robotViewID + 61)!!
                        newView = activity?.findViewById(robotViewID + 82)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 1, robotViewID + 22, robotViewID - 20,
                            robotViewID + 21, robotViewID - 21, robotViewID + 20,
                            robotViewID - 1, robotViewID - 22, robotViewID + 19,
                            robotViewID - 2, robotViewID - 23, robotViewID + 18,
                            robotViewID - 3, robotViewID - 24
                        ))
                        highlightMainPath(arrayOf(robotViewID -1, robotViewID-2, robotViewID+19, robotViewID+40, robotViewID+61, robotViewID, robotViewID+82))
                    }

                    // down
                    // main path checked
                    4 -> {
                        newView = activity?.findViewById(robotViewID + 1)!!
                        highlightPath(arrayOf(robotViewID + 20, robotViewID - 1, robotViewID - 22))
                        highlightMainPath(arrayOf(robotViewID + 1, robotViewID))
                    }

                    // down left
                    //main path checked
                    5 -> {
                        newRobotCenter = activity?.findViewById(robotViewID - 38)!!
                        newView = activity?.findViewById(robotViewID - 17)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 21, robotViewID + 1, robotViewID + 22,
                            robotViewID + 2, robotViewID + 23, robotViewID + 3,
                            robotViewID + 24, robotViewID + 4, robotViewID + 25,
                            robotViewID + 5, robotViewID + 26, robotViewID - 19,
                            robotViewID - 20, robotViewID - 21, robotViewID - 22,
                            robotViewID - 1, robotViewID + 20
                        ))
                        highlightMainPath(arrayOf(robotViewID +1, robotViewID, robotViewID+2, robotViewID+3, robotViewID+4, robotViewID-17, robotViewID-38))
                    }

                    // down right
                    //main path checked
                    6 -> {
                        newRobotCenter = activity?.findViewById(robotViewID + 46)!!
                        newView = activity?.findViewById(robotViewID + 25)!!
                        viewModel.setRobotPosition(arrayOf((newRobotCenter.tag as GridPoint).xPos, (newRobotCenter.tag as GridPoint).yPos))
                        highlightPath(arrayOf(
                            robotViewID + 1, robotViewID + 2, robotViewID + 3,
                            robotViewID + 4, robotViewID + 5, robotViewID - 21,
                            robotViewID - 20, robotViewID - 19, robotViewID - 18,
                            robotViewID - 17, robotViewID - 16, robotViewID - 22,
                            robotViewID - 1, robotViewID + 20, robotViewID + 21,
                            robotViewID + 22, robotViewID + 23
                        ))
                        highlightMainPath(arrayOf(robotViewID +1, robotViewID, robotViewID+2, robotViewID+3, robotViewID+4, robotViewID+25, robotViewID+46))
                    }

                    //on the spot left
                    //main path checked
                    7 -> {
                        newView = activity?.findViewById(robotViewID - 21)!!
                        highlightMainPath(arrayOf(robotViewID))
                    }

                    //on the spot right
                    //main path checked
                    8 -> {
                        newView = activity?.findViewById(robotViewID + 21)!!
                        highlightMainPath(arrayOf(robotViewID))
                    }
                }
            }
        }

        // called when goForwardLeft or goForwardRight, backLeft, backRight
        if (directionType == 2 || directionType == 3 || directionType == 5 || directionType == 6) {
            // set to new position
            moveRobot(oldCenter, newRobotCenter)
            // Rotate robot left/right
            rotateItem(newView as View)
            newView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }

        //called when rotating on the spot
        else if (directionType == 7 || directionType == 8){
            //only need to redraw other grids
            setRobotOtherGrids(oldCenter.id, R.color.white, false)
            rotateItem(newView as View)
            newView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
        // called when goUp or goDown
        else if (directionType == 1 || directionType == 4) {
            moveRobot(oldCenter, newView)
            Log.d(TAG, viewModel.robotDirection.value.toString())
            // set position to the new center position
            if (newView.tag != null)
                viewModel.setRobotPosition(arrayOf((newView.tag as GridPoint).xPos, (newView.tag as GridPoint).yPos))
        }
    }

    fun goUp() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 1)
    }

    fun goForwardLeft() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 2)
    }

    fun goForwardRight() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 3)
    }

    fun goDown() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 4)
    }

    fun goBackLeft() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 5)
    }

    fun goBackRight() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 6)
    }

    fun onTheSpotLeft() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 7)
    }

    fun onTheSpotRight() {
        val direction = viewModel.robotDirection.value.toString()
        robotCurrentDirectionMovement(direction, 8)
    }

    private fun moveRobot(oldCenter: TextView, newCenter: TextView) {
        var gridData = GridPoint()

        // set old center as highlighted grid
        oldCenter.setBackgroundColor(ContextCompat.getColor(
            requireContext(),
            R.color.pink_700
        ))

        // Draw robot at new position and orientation
        newCenter.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        if (newCenter.tag != null) gridData = newCenter.tag as GridPoint
        gridData.value = Constants.Companion.GridPointType.ROBOT_CENTER
        newCenter.setOnLongClickListener(this)
        setRobotOtherGrids(newCenter.id, R.color.white, false)
    }

    // ------Drawing, rotating and clearing functions------ //

    fun mapConfig1() {

        resetMap()

        val v = activity?.findViewById<TextView>(mapDetailsIDs[2][2])
        (v!!.tag as GridPoint).value = Constants.Companion.GridPointType.ROBOT_CENTER
        viewModel.setRobotDirection(getString(R.string.robot_direction_north))
        viewModel.setRobotPosition(arrayOf(2, 2))

        val v1 = activity?.findViewById<TextView>(mapDetailsIDs[14][2])
        (v1!!.tag as GridPoint).textInside = 0
        (v1.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_RIGHT

        val v2 = activity?.findViewById<TextView>(mapDetailsIDs[17][9])
        (v2!!.tag as GridPoint).textInside = 1
        (v2.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_BOTTOM

        val v3 = activity?.findViewById<TextView>(mapDetailsIDs[6][8])
        (v3!!.tag as GridPoint).textInside = 2
        (v3.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_TOP

        val v4 = activity?.findViewById<TextView>(mapDetailsIDs[15][17])
        (v4!!.tag as GridPoint).textInside = 3
        (v4.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_BOTTOM

        val v5 = activity?.findViewById<TextView>(mapDetailsIDs[6][10])
        (v5!!.tag as GridPoint).textInside = 4
        (v5.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_RIGHT

        val v6 = activity?.findViewById<TextView>(mapDetailsIDs[0][19])
        (v6!!.tag as GridPoint).textInside = 5
        (v6.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_LEFT

        v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        drawRobot(v)

        drawObstacle(v1, R.drawable.east_gradient_background)
        viewModel.addGridPointToArray((v1.tag as GridPoint))

        drawObstacle(v2, R.drawable.south_gradient_background)
        viewModel.addGridPointToArray((v2.tag as GridPoint))

        drawObstacle(v3, R.drawable.north_gradient_background)
        viewModel.addGridPointToArray((v3.tag as GridPoint))

        drawObstacle(v4, R.drawable.south_gradient_background)
        viewModel.addGridPointToArray((v4.tag as GridPoint))

        drawObstacle(v5, R.drawable.east_gradient_background)
        viewModel.addGridPointToArray((v5.tag as GridPoint))

        drawObstacle(v6, R.drawable.west_gradient_background)
        viewModel.addGridPointToArray((v6.tag as GridPoint))

        obstacleCount = 6
        activity?.findViewById<TextView>(R.id.num_of_obstacles)?.text = "6"
    }

    fun mapConfig2() {

        resetMap()

        val v = activity?.findViewById<TextView>(mapDetailsIDs[2][2])
        (v!!.tag as GridPoint).value = Constants.Companion.GridPointType.ROBOT_CENTER
        viewModel.setRobotDirection(getString(R.string.robot_direction_north))
        viewModel.setRobotPosition(arrayOf(2, 2))

        val v1 = activity?.findViewById<TextView>(mapDetailsIDs[13][1])
        (v1!!.tag as GridPoint).textInside = 0
        (v1.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_TOP

        val v2 = activity?.findViewById<TextView>(mapDetailsIDs[17][12])
        (v2!!.tag as GridPoint).textInside = 1
        (v2.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_RIGHT

        val v3 = activity?.findViewById<TextView>(mapDetailsIDs[5][5])
        (v3!!.tag as GridPoint).textInside = 2
        (v3.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_TOP

        val v4 = activity?.findViewById<TextView>(mapDetailsIDs[7][10])
        (v4!!.tag as GridPoint).textInside = 3
        (v4.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_BOTTOM

        val v5 = activity?.findViewById<TextView>(mapDetailsIDs[13][17])
        (v5!!.tag as GridPoint).textInside = 4
        (v5.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_BOTTOM

        val v6 = activity?.findViewById<TextView>(mapDetailsIDs[0][10])
        (v6!!.tag as GridPoint).textInside = 5
        (v6.tag as GridPoint).value = Constants.Companion.GridPointType.OBSTACLE_TOP

        v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        drawRobot(v)

        drawObstacle(v1, R.drawable.east_gradient_background)
        viewModel.addGridPointToArray((v1.tag as GridPoint))

        drawObstacle(v2, R.drawable.east_gradient_background)
        viewModel.addGridPointToArray((v2.tag as GridPoint))

        drawObstacle(v3, R.drawable.north_gradient_background)
        viewModel.addGridPointToArray((v3.tag as GridPoint))

        drawObstacle(v4, R.drawable.south_gradient_background)
        viewModel.addGridPointToArray((v4.tag as GridPoint))

        drawObstacle(v5, R.drawable.south_gradient_background)
        viewModel.addGridPointToArray((v5.tag as GridPoint))

        drawObstacle(v6, R.drawable.north_gradient_background)
        viewModel.addGridPointToArray((v6.tag as GridPoint))

        obstacleCount = 6
        activity?.findViewById<TextView>(R.id.num_of_obstacles)?.text = "6"
    }

    private fun drawRobot(v: TextView) {
        setRobotOtherGrids(v.id, R.color.white, false)
        checkAndUpdateMap(v)
        viewModel.addStatusText(
            "ADD, Robot, (${(v.tag as GridPoint).xPos}, ${(v.tag as GridPoint).yPos})"
        )
    }

    private fun drawObstacle(v: TextView, gradient: Int) {
        v.setOnClickListener(this)
        v.text = (v.tag as GridPoint).textInside.toString()
        v.gravity = Gravity.CENTER
        v.setBackground(ContextCompat.getDrawable(requireContext(), gradient))
        val char = checkAndUpdateMap(v)
        viewModel.addStatusText("ADD, O${(v.tag as GridPoint).textInside}," +
                " (${(v.tag as GridPoint).xPos}, ${(v.tag as GridPoint).yPos})")
        viewModel.addStatusText("FACE, O${(v.tag as GridPoint).textInside}, $char")
        obstacleInfoOnAction(v, 1)
    }

    private fun rotateItem(v: View) {
        var gridData = GridPoint()

        if (v.tag != null) gridData = v.tag as GridPoint
        var points = arrayOf(0, 0, 0)

        v as TextView

        when (gridData.value) {
            Constants.Companion.GridPointType.ROBOT_TOP -> {
                points = arrayOf((v.id - 20), ((v.id - 20) - 22), (((v.id - 20) - 22) + 20))
                viewModel.setRobotDirection(getString(R.string.robot_direction_north))
            }
            Constants.Companion.GridPointType.ROBOT_LEFT -> {
                points = arrayOf((v.id + 22), ((v.id + 22) - 20), (((v.id + 22) - 20) - 22))
                viewModel.setRobotDirection(getString(R.string.robot_direction_west))
            }
            Constants.Companion.GridPointType.ROBOT_BOTTOM -> {
                points = arrayOf((v.id + 20), ((v.id + 20) + 22), (((v.id + 20) + 22) - 20))
                viewModel.setRobotDirection(getString(R.string.robot_direction_south))
            }
            Constants.Companion.GridPointType.ROBOT_RIGHT -> {
                points = arrayOf((v.id - 22), ((v.id - 22) + 20), (((v.id - 22) + 20) + 22))
                viewModel.setRobotDirection(getString(R.string.robot_direction_east))
            }
            else -> {
                when (gridData.value) {
                    Constants.Companion.GridPointType.OBSTACLE_RIGHT -> {
                        //rotate to south
                        v.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.south_gradient_background))
                        gridData.value = Constants.Companion.GridPointType.OBSTACLE_BOTTOM
                        viewModel.addGridPointToArray(gridData)
                    }
                    Constants.Companion.GridPointType.OBSTACLE_LEFT -> {
                        //rotate to north
                        v.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.north_gradient_background))
                        gridData.value = Constants.Companion.GridPointType.OBSTACLE_TOP
                        viewModel.addGridPointToArray(gridData)
                    }
                    Constants.Companion.GridPointType.OBSTACLE_BOTTOM -> {
                        //rotate to west
                        v.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.west_gradient_background))
                        gridData.value = Constants.Companion.GridPointType.OBSTACLE_LEFT
                        viewModel.addGridPointToArray(gridData)
                    }
                    Constants.Companion.GridPointType.OBSTACLE_TOP -> {
                        //rotate to east
                        v.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.east_gradient_background))
                        gridData.value = Constants.Companion.GridPointType.OBSTACLE_RIGHT
                        viewModel.addGridPointToArray(gridData)
                    }
                    Constants.Companion.GridPointType.ROBOT_CENTER,
                    Constants.Companion.GridPointType.ROBOT_OTHER,
                    Constants.Companion.GridPointType.EMPTY,
                    Constants.Companion.GridPointType.EDGE -> {
                        return
                    }
                    else -> {}
                }
            }
        }

        if (gridData.value in
            listOf(Constants.Companion.GridPointType.ROBOT_TOP,
                Constants.Companion.GridPointType.ROBOT_RIGHT,
            Constants.Companion.GridPointType.ROBOT_BOTTOM,
            Constants.Companion.GridPointType.ROBOT_LEFT)) {
            // For rotating of robot
            activity?.findViewById<TextView>(v.id)
                ?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
            for (point in points) {
                activity?.findViewById<TextView>(point)
                    ?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
        else {
            val char = checkAndUpdateMap(v)
            viewModel.addStatusText("FACE, O${(v.tag as GridPoint).textInside}, $char")
            obstacleInfoOnAction(v, 2)
        }
    }

    private fun clearItem(v: View) {
        Log.v("SR", "clearItem() called")

        val dragData = v.tag as GridPoint
        val clipText = dragData.value.ordinal.toString()

        // If robot is clicked, clear the grids for the robot edges
        if (dragData.value == Constants.Companion.GridPointType.ROBOT_CENTER) {
            setRobotOtherGrids(v.id, R.color.light_purple, true)
        }
        else {
            v as TextView
            viewModel.addStatusText("SUB, O${(v.tag as GridPoint).textInside}")
            viewModel.removeGridPointFromArray(dragData)
        }

        val item = ClipData.Item(clipText)
        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val data = ClipData(clipText, mimeTypes, item)

        val dragShadowBuilder = View.DragShadowBuilder(v)
        v.startDragAndDrop(data, dragShadowBuilder, v, 0)
    }

    // ------Misc map-related functions------ //

     fun updateObstacleImage(xPos: Int, yPos: Int, image: Int, obstacleNumber: Int, obstacleDirection: Constants.Companion.GridPointType) {

         var id = mapDetailsIDs[yPos][xPos]
         val v = activity?.findViewById<TextView>(id)
         if (image == 9 || image == 41) {
             (v as TextView).text = ""
         } else {
             clearItem(v as TextView) //removes obstacle from view model and clears drawing

             (v.tag as GridPoint).textInside = image //Update tag's text to display
             v.text = (v.tag as GridPoint).textInside.toString()
             //v.textSize = 16F // Increase size of text

             val gradient = when ((v.tag as GridPoint).value) {
                 Constants.Companion.GridPointType.OBSTACLE_TOP -> R.drawable.north_gradient_background
                 Constants.Companion.GridPointType.OBSTACLE_RIGHT -> R.drawable.east_gradient_background
                 Constants.Companion.GridPointType.OBSTACLE_LEFT -> R.drawable.west_gradient_background
                 Constants.Companion.GridPointType.OBSTACLE_BOTTOM -> R.drawable.south_gradient_background
                 else -> R.drawable.north_gradient_background
             }
             drawObstacle(v, gradient) // Re-draws obstacle

             viewModel.readdGridPointToArray(
                 v.tag as GridPoint,
                 obstacleNumber
             ) //Re-add obstacle at original position
         }
     }

    fun updateRobotPositionAndFacingDirection(xPos: Int, yPos: Int, direction: String) {

        // Clear previous robot location first

        var currentRobotPosition: Array<Int> = viewModel.robotPosition.value!!
        var robotView = activity?.findViewById<TextView>(mapDetailsIDs[currentRobotPosition[1]][currentRobotPosition[0]])
        clearItem(robotView!!)

        val dragData = robotView?.tag as GridPoint

        //Change colour back to purple and set grid type to EMPTY and textview text to empty
        robotView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.light_purple
            )
        )
        dragData.value = Constants.Companion.GridPointType.EMPTY
        robotView.text = ""

        // Draw robot at new location next

        var id = mapDetailsIDs[yPos][xPos]
        var v = activity?.findViewById<TextView>(id)
        val directionPoints = arrayOf((id + 21), (id + 1), (id - 21), (id - 1))
        // draw robot
        v?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        drawRobot(v!!)

        v = activity?.findViewById(directionPoints[0])
        // set to white for north direction initially
        v?.setBackgroundColor(Color.WHITE)

        // set position
        viewModel.setRobotPosition(arrayOf(xPos, yPos))
        // set direction
        viewModel.setRobotDirection(direction)
        // check current direction of robot get id of direction textview
        id = when (viewModel.robotDirection.value.toString()) {
            getString(R.string.robot_direction_east) -> directionPoints[1]
            getString(R.string.robot_direction_south) -> directionPoints[2]
            getString(R.string.robot_direction_west) -> directionPoints[3]
            else -> directionPoints[0]
        }

        v = activity?.findViewById(id)
        // set color to black
        v?.setBackgroundColor(Color.BLACK)
    }

    //convert status to char for textview that is passed in and update mapDetails
    private fun checkAndUpdateMap(v: TextView): Char {

        var transformedChar = ' '

        if (v.tag != null) {

            transformedChar = when ((v.tag as GridPoint).value) {
                Constants.Companion.GridPointType.OBSTACLE_LEFT -> 'L'
                Constants.Companion.GridPointType.OBSTACLE_RIGHT -> 'R'
                Constants.Companion.GridPointType.OBSTACLE_TOP -> 'U'
                Constants.Companion.GridPointType.OBSTACLE_BOTTOM -> 'D'
                Constants.Companion.GridPointType.ROBOT_CENTER -> 'C'
                Constants.Companion.GridPointType.ROBOT_OTHER -> 'O'
                Constants.Companion.GridPointType.ROBOT_LEFT -> 'W'
                Constants.Companion.GridPointType.ROBOT_RIGHT -> 'E'
                Constants.Companion.GridPointType.ROBOT_TOP -> 'N'
                Constants.Companion.GridPointType.ROBOT_BOTTOM -> 'S'
                Constants.Companion.GridPointType.ROBOT_ONTHESPOTLEFT -> 'T'
                Constants.Companion.GridPointType.ROBOT_ONTHESPOTRIGHT -> 'Y'
                else -> 'X'
            }
            mapDetails[v.id] = transformedChar


//            Log.v(
//                "DATA", "ID: ${v.id}, " +
//                        "X: ${(v.tag as GridPoint).xPos} " +
//                        "Y: ${(v.tag as GridPoint).yPos} " +
//                        "STATUS: ${(v.tag as GridPoint).xPos}," +
//                        "CHAR: $transformedChar"
//            )
        }
        return transformedChar
    }

    private fun resetMap() {

        for (row in mapDetailsIDs) {
            for (column in row) {
                val textView = activity?.findViewById<TextView>(column)
                // initialize tag to empty grid point
                // set value to empty, remove drawable and text
                (textView?.tag as GridPoint).value = Constants.Companion.GridPointType.EMPTY
                textView.text = ""
                // enable robot button
                activity?.findViewById<ImageButton>(R.id.robotControl)?.isEnabled = true
                // set robot direction to default north
                viewModel.setRobotDirection(getString(R.string.robot_direction_north))
                // update map details for each view, setting each to X
                checkAndUpdateMap(textView)
                // change color to default purple
                textView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.light_purple
                    )
                )
            }
        }

        //set obstacle count to 0
        obstacleCount = 0
        // current grid and obstacle count text to empty
        activity?.findViewById<TextView>(R.id.current_grid_x_y)?.text = ""
        activity?.findViewById<TextView>(R.id.num_of_obstacles)?.text = ""
        viewModel.resetArrayOfGridPoints()
        val obstaclesLayout = activity?.findViewById<LinearLayout>(R.id.current_obstacles_info)
        obstaclesLayout?.removeAllViews()
    }

    private fun highlightPath(highLightViewIDs: Array<Int>) {
        // the views that are going to be highlighted on movement and rotation
        for (viewId in highLightViewIDs) {
            if (viewId in usedCells){
                activity?.findViewById<TextView>(viewId)?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.light_pink
                    )
                )
            }
            else activity?.findViewById<TextView>(viewId)?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_700
                )
            )
        }
    }

    private fun highlightMainPath(highLightViewIDs: Array<Int>) {
        // the views that are going to be highlighted on movement and rotation
        for (viewId in highLightViewIDs) {
            activity?.findViewById<TextView>(viewId)?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_pink
                )
            )

            usedCells += viewId
        }
    }

    private fun obstacleInfoOnAction(v: TextView, addOrRotate: Int) {
        val obstaclesLayout = activity?.findViewById<LinearLayout>(R.id.current_obstacles_info)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(4, 4, 4, 4)

        val textView = TextView(context)
        textView.tag = v.tag
        textView.text = v.text
        textView.append("\n${(v.tag as GridPoint).xPos}, ${(v.tag as GridPoint).yPos}")
        textView.setTextColor(v.currentTextColor)
        textView.gravity = Gravity.CENTER
        textView.setPadding(16)
        textView.background = v.background
        obstaclesLayout?.addView(textView, lp)

        if (addOrRotate == 2) { // rotate then check if it exists in layout, if it exists, remove it
            for (view in obstaclesLayout?.children!!) {
                if (view.tag != null) {
                    if ((view.tag as GridPoint).textInside == (v.tag as GridPoint).textInside) {
                        obstaclesLayout.removeView(view)
                    }
                }
            }
        }
        else { // check if textInside is the same and position is different, if it is, remove it
            for (view in obstaclesLayout?.children!!) {
                if (view.tag != null) {
                    if (((view.tag as GridPoint).textInside == (v.tag as GridPoint).textInside &&
                        (view.tag as GridPoint).xPos != (v.tag as GridPoint).xPos) ||
                        ((view.tag as GridPoint).textInside == (v.tag as GridPoint).textInside &&
                        (view.tag as GridPoint).yPos != (v.tag as GridPoint).yPos)) {
                        obstaclesLayout.removeView(view)
                    }
                }
                else {
                    Log.d("CLEAR", "SHOULD ADD IN")
                }
            }
        }
    }

    // For debugging purposes, check current clicked item
    private fun debugCurrentItem(v: View) {
        val gridData = v.tag as GridPoint
        Log.v("SR", "textview details:")
        Log.v("SR", v.id.toString())
        Log.v("SR", gridData.value.toString())
        Log.v("SR", gridData.textInside.toString())
        Log.v("SR", gridData.xPos.toString() + ", " + gridData.yPos.toString())
    }

    // For debugging purposes, check current map
    private fun printMap() {
        Log.v("Grid Map", mapDetails.toString())
    }

}