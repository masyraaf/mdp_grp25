package com.group6.mdpandroid.algorithm

import android.util.Log
import com.group6.mdpandroid.entity.GridPoint
import com.group6.mdpandroid.utils.Constants
import kotlin.collections.ArrayList

class GridDetails(obstacleList: ArrayList<GridPoint>) {

    private var arenaCells: Array<Array<ArenaCell?>> = Array(Constants.GRID_WIDTH) { col ->
        Array(Constants.GRID_HEIGHT) { row ->
            ArenaCell(col, row, Direction.NA)
        }
    }

    // Initial robot coordinates and direction
    private var robotCenterRow: Int = 2
    private var robotCenterCol: Int = 2
    private var robotDirection: Direction = Direction.NORTH
    var imageCellsToTake = arrayListOf<ArenaCell>()

    init {
        setCellDetails(obstacleList)
    }

    private fun setCellDetails(obstacleList: ArrayList<GridPoint>){

        for (a in 0 until obstacleList.size){
            var obstacle = obstacleList[a]
            var xPos = obstacle.xPos
            var yPos = obstacle.yPos
            (arenaCells[xPos][yPos])?.setIsObstacle(true)
            when (obstacle.value){
                Constants.Companion.GridPointType.OBSTACLE_RIGHT -> {
                    (arenaCells[xPos][yPos])?.setImageDirection(Direction.EAST)
                }
                Constants.Companion.GridPointType.OBSTACLE_LEFT -> {
                    (arenaCells[xPos][yPos])?.setImageDirection(Direction.WEST)
                }
                Constants.Companion.GridPointType.OBSTACLE_TOP -> {
                    (arenaCells[xPos][yPos])?.setImageDirection(Direction.NORTH)
                }
                Constants.Companion.GridPointType.OBSTACLE_BOTTOM -> {
                    (arenaCells[xPos][yPos])?.setImageDirection(Direction.SOUTH)
                }
                else -> {}
            }
            imageCellsToTake.add((arenaCells[xPos][yPos])!!)
        }

        var obstacleList = arrayListOf<ArenaCell>()
        var vobstacleList = arrayListOf<ArenaCell>()

        //Create List of Obstacles
        for (x in 0..19)
            for (y in 0..19){
                if(arenaCells[x][y]?.isObstacle() == true) {
                    var arenaCell = ArenaCell(x,y, Direction.NA)
                    obstacleList.add(arenaCell)
                }
            }
        Log.v("Obstacle List", obstacleList.count().toString())

         //Setting Virtual Walls around the perimeter
         for (x in 0..19)
        {
            arenaCells[x][0]?.setVirtualWall(true)
            arenaCells[x][19]?.setVirtualWall(true)
        }

        for (y in 0..19)
        {
            arenaCells[0][y]?.setVirtualWall(true)
            arenaCells[19][y]?.setVirtualWall(true)
        }
        // Setting Virtual Obstacles around the obstacle

        for (arenaCell in obstacleList){
            var obstacle = arenaCell
            var row = obstacle.getIndexRow()
            var col = obstacle.getIndexColumn()

            // Full flower
            if (row in 2..17 && col in 2..17){
                surroundVirtual(col,row)
                (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
            }

            // Flower except for one side
            else if (row in 1..18 && col in 1..18){
                surroundVirtual(col,row)
                if (row == 1){
                    (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                    if (col == 1){
                         (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                    }
                    else if (col == 18){
                        (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                    }
                    else{
                        (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                        (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                    }
                }

                if (row == 18){
                    (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                    if (col == 1){
                        (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                    }
                    else if (col == 18){
                        (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                    }
                    else{
                        (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                        (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                    }
                }
            }

            // bottom edges except corner
            else if (row == 0 && col != 0 && col != 19 ) {
                (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                (arenaCells[col-1][row+1])?.setVirtualObstacle(true) //topleft
                (arenaCells[col+1][row+1])?.setVirtualObstacle(true) //topright
                (arenaCells[col-1][row])?.setVirtualObstacle(true) //middleleft
                (arenaCells[col+1][row])?.setVirtualObstacle(true) //middleright
                (arenaCells[col][row+1])?.setVirtualObstacle(true) //topmiddle

                if (col == 1) {
                    (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right

                } else if (col == 18) {
                    (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                } else {
                    (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                    (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                }
            }

            // top edges except corner
            else if (row == 19 && col != 0 && col != 19){
                (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                (arenaCells[col-1][row-1])?.setVirtualObstacle(true) //bottomleft
                (arenaCells[col+1][row-1])?.setVirtualObstacle(true) //bottomright
                (arenaCells[col-1][row])?.setVirtualObstacle(true) //middleleft
                (arenaCells[col+1][row])?.setVirtualObstacle(true) //middleright
                (arenaCells[col][row-1])?.setVirtualObstacle(true) //bottommiddle

                if (col == 1) {
                    (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right

                } else if (col == 18) {
                    (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                } else {
                    (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                    (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                }
            }

            // left edges except corner
            else if (col == 0 && row != 0 && row != 19){
                (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
                (arenaCells[col][row+1])?.setVirtualObstacle(true) //topmiddle
                (arenaCells[col+1][row+1])?.setVirtualObstacle(true) //topright
                (arenaCells[col+1][row-1])?.setVirtualObstacle(true) //bottomright
                (arenaCells[col+1][row])?.setVirtualObstacle(true) //middleright
                (arenaCells[col][row-1])?.setVirtualObstacle(true) //bottommiddle

                if (row == 1) {
                    (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                } else if (row == 18){
                    (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                } else {
                    (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                    (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                }
            }

            // right edges except corner
            else if (col == 19 && row != 0 && row != 19){
                (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                (arenaCells[col][row+1])?.setVirtualObstacle(true) //topmiddle
                (arenaCells[col-1][row+1])?.setVirtualObstacle(true) //topleft
                (arenaCells[col-1][row-1])?.setVirtualObstacle(true) //bottomleft
                (arenaCells[col-1][row])?.setVirtualObstacle(true) //middleleft
                (arenaCells[col][row-1])?.setVirtualObstacle(true) //bottommiddle

                if (row == 1) {
                    (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                } else if (row == 19){
                    (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                } else {
                    (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                    (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                }
            }

           // bottom left corner and bottom right corner
           else if (row == 0){
                    (arenaCells[col][row+1])?.setVirtualObstacle(true) //topmiddle
                    (arenaCells[col][row+2])?.setVirtualObstacle(true) //extra up
                if (col == 0){
                    (arenaCells[col+1][row+1])?.setVirtualObstacle(true) //topright
                    (arenaCells[col+1][row])?.setVirtualObstacle(true) //middleright
                    (arenaCells[col+2][row])?.setVirtualObstacle(true) //extra right
               }
                else if (col == 19){
                    (arenaCells[col-1][row+1])?.setVirtualObstacle(true) //topleft
                    (arenaCells[col-2][row])?.setVirtualObstacle(true) //extra left
                    (arenaCells[col-1][row])?.setVirtualObstacle(true) //middleleft
                }
            }

           // top left corner and top right corner
           else if (row == 19) {
                (arenaCells[col][row-2])?.setVirtualObstacle(true) //extra down
                (arenaCells[col][row-1])?.setVirtualObstacle(true) //bottommiddle

                if (col == 0) {
                    (arenaCells[col+1][row-1])?.setVirtualObstacle(true) //bottomright
                    (arenaCells[col + 1][row])?.setVirtualObstacle(true) //middleright
                    (arenaCells[col + 2][row])?.setVirtualObstacle(true) //extra right
                } else if (col == 19) {
                    (arenaCells[col-1][row-1])?.setVirtualObstacle(true) //bottomleft
                    (arenaCells[col - 2][row])?.setVirtualObstacle(true) //extra left
                    (arenaCells[col - 1][row])?.setVirtualObstacle(true) //middleleft
                }
            }
        }

        for (x in 0..19)
            for (y in 0..19){
                if(arenaCells[x][y]?.isVirtualObstacle() == true) {
                    Log.v("Area around Obstacle", "x:${x}, y: ${y}")
                    var arenaCell = ArenaCell(x,y, Direction.NA)
                    vobstacleList.add(arenaCell)
                }
            }
        Log.v("VObstacle List", vobstacleList.count().toString())
    }

    private fun surroundVirtual(col: Int, row: Int){
        (arenaCells[col+1][row-1])?.setVirtualObstacle(true) //bottomright
        (arenaCells[col+1][row+1])?.setVirtualObstacle(true) //topright
        (arenaCells[col-1][row-1])?.setVirtualObstacle(true) //bottomleft
        (arenaCells[col-1][row+1])?.setVirtualObstacle(true) //topleft
        (arenaCells[col][row-1])?.setVirtualObstacle(true) //bottommiddle
        (arenaCells[col][row+1])?.setVirtualObstacle(true) //topmiddle
        (arenaCells[col+1][row])?.setVirtualObstacle(true) //middleright
        (arenaCells[col-1][row])?.setVirtualObstacle(true) //middleleft
    }

    fun getAllUntakenImageCells(): ArrayList<ArenaCell> {
        return imageCellsToTake
    }

    fun getArenaCell(col: Int, row: Int): ArenaCell {
        return arenaCells[col][row]!!
    }

    fun getRobotCenterRow(): Int{
        return robotCenterRow
    }

    fun setRobotCenterRow(i: Int){
        robotCenterRow = i
    }

    fun getRobotCenterCol(): Int{
        return robotCenterCol
    }

    fun setRobotCenterCol(i: Int){
        robotCenterCol = i
    }

    fun getRobotDirection(): Direction{
        return robotDirection
    }

    fun setRobotDirection(d: Direction) {
        robotDirection = d
    }

}