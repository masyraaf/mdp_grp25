package com.group6.mdpandroid.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group6.mdpandroid.entity.GridPoint

class RobotMapViewModel : ViewModel() {

    private val TAG = "RobotMapViewModel"

    //=====================Map Variables and Functions=====================//

    var arrayOfGridPoint = ArrayList<GridPoint>()
    val arrayOfGridPoints: ArrayList<GridPoint> get() = arrayOfGridPoint

    // Called when a new obstacle is added or an existing obstacle is rotated
    fun addGridPointToArray(g: GridPoint) {
        for (a in arrayOfGridPoints){
            // For rotating of existing obstacle, remove previous GridPoint as well
            if (a.xPos == g.xPos && a.yPos == g.yPos){
                arrayOfGridPoints.removeAt(arrayOfGridPoints.indexOf(a))
                break
            }
        }
        arrayOfGridPoints.add(g)
    }

    fun readdGridPointToArray(g: GridPoint, index: Int) {
        arrayOfGridPoints.add(index, g)
    }

    // Called when an obstacle is dragged out of the GridMap
    fun removeGridPointFromArray(g: GridPoint) {
        for (a in arrayOfGridPoints){
            if (a.xPos == g.xPos && a.yPos == g.yPos){
                arrayOfGridPoints.removeAt(arrayOfGridPoints.indexOf(a))
                break
            }
        }
    }

    // Called when the grid map is reset
    fun resetArrayOfGridPoints(){
        arrayOfGridPoints.clear()
    }

    private val mutableLeftForwardToggle = MutableLiveData<Boolean>()
    val leftForwardToggle: LiveData<Boolean> get() = mutableLeftForwardToggle
    fun setLeftForwardToggle() {
        if (mutableLeftForwardToggle.value == null) {
            mutableLeftForwardToggle.value = true
        } else {
            mutableLeftForwardToggle.value?.let {
                mutableLeftForwardToggle.value = !it
            }
        }
    }

    private val mutableRightForwardToggle = MutableLiveData<Boolean>()
    val rightForwardToggle: LiveData<Boolean> get() = mutableRightForwardToggle
    fun setRightForwardToggle() {
        if (mutableRightForwardToggle.value == null) {
            mutableRightForwardToggle.value = true
        } else {
            mutableRightForwardToggle.value?.let {
                mutableRightForwardToggle.value = !it
            }
        }
    }

    private val mutableLeftBackToggle = MutableLiveData<Boolean>()
    val leftBackToggle: LiveData<Boolean> get() = mutableLeftBackToggle
    fun setLeftBackToggle() {
        if (mutableLeftBackToggle.value == null) {
            mutableLeftBackToggle.value = true
        } else {
            mutableLeftBackToggle.value?.let {
                mutableLeftBackToggle.value = !it
            }
        }
    }

    private val mutableRightBackToggle = MutableLiveData<Boolean>()
    val rightBackToggle: LiveData<Boolean> get() = mutableRightBackToggle
    fun setRightBackToggle() {
        if (mutableRightBackToggle.value == null) {
            mutableRightBackToggle.value = true
        } else {
            mutableRightBackToggle.value?.let {
                mutableRightBackToggle.value = !it
            }
        }
    }

    private val mutableUpToggle = MutableLiveData<Boolean>()
    val upToggle: LiveData<Boolean> get() = mutableUpToggle
    fun setUpToggle() {
        if (mutableUpToggle.value == null) {
            mutableUpToggle.value = true
        } else {
            mutableUpToggle.value?.let {
                mutableUpToggle.value = !it
            }
        }
    }

    private val mutableDownToggle = MutableLiveData<Boolean>()
    val downToggle: LiveData<Boolean> get() = mutableDownToggle
    fun setDownToggle() {
        if (mutableDownToggle.value == null) {
            mutableDownToggle.value = true
        } else {
            mutableDownToggle.value?.let {
                mutableDownToggle.value = !it
            }
        }
    }

    private val mutableOnTheSpotLeftToggle = MutableLiveData<Boolean>()
    val onTheSpotLeftToggle: LiveData<Boolean> get() = mutableOnTheSpotLeftToggle
    fun setOnTheSpotLeftToggle() {
        if (mutableOnTheSpotLeftToggle.value == null) {
            mutableOnTheSpotLeftToggle.value = true
        } else {
            mutableOnTheSpotLeftToggle.value?.let {
                mutableOnTheSpotLeftToggle.value = !it
            }
        }
    }

    private val mutableOnTheSpotRightToggle = MutableLiveData<Boolean>()
    val onTheSpotRightToggle: LiveData<Boolean> get() = mutableOnTheSpotRightToggle
    fun setOnTheSpotRightToggle() {
        if (mutableOnTheSpotRightToggle.value == null) {
            mutableOnTheSpotRightToggle.value = true
        } else {
            mutableOnTheSpotRightToggle.value?.let {
                mutableOnTheSpotRightToggle.value = !it
            }
        }
    }
    //====================Robot Variables and Functions====================//

    // Robot Position -> e.g. [3,4]
    // Stores the current robot position on the map as an array of its coordinates
    private val mutableRobotPosition = MutableLiveData<Array<Int>>()
    val robotPosition: LiveData<Array<Int>> get() = mutableRobotPosition
    fun setRobotPosition(position: Array<Int>) {
        mutableRobotPosition.value = position
    }

    // Robot Direction -> e.g. "NORTH"
    // Stores the current direction the robot is facing on the map
    private val mutableRobotDirection = MutableLiveData<String>()
    val robotDirection: LiveData<String> get() = mutableRobotDirection
    fun setRobotDirection(direction: String) {
        mutableRobotDirection.value = direction
    }

    // Status Texts -> e.g. ["Ready to start.", "Looking for target."]
    // Stores the array of statuses for the robot for display in a list
    private val mutableStatusTexts = MutableLiveData<String>()
    val statusTexts: LiveData<String> get() = mutableStatusTexts
    fun addStatusText(newStatus: String) {
        Log.d(TAG, "Status for Added Items : $newStatus")
        mutableStatusTexts.value = newStatus
    }

    //====================Task Variables and Functions====================//

    // Current Task -> e.g. 1
    // Stores the current task, refer to Constants.kt for task numbers
    private val mutableCurrentTask = MutableLiveData<Int>()
    val currentTask: LiveData<Int> get() = mutableCurrentTask
    fun setCurrentTask(task: Int) {
        mutableCurrentTask.value = task
    }

    // Toggles when the chronometer should run
    private val mutableTimeStarted = MutableLiveData<Boolean>()
    val timeStarted: LiveData<Boolean> get() = mutableTimeStarted
    fun setTimeStarted(started: Boolean) {
        mutableTimeStarted.value = started
    }

}
