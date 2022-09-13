package com.group6.mdpandroid.algorithm

import android.util.Log
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

class FastestPath4(gridDetails: GridDetails, destination: ArenaCell) {

    private var destination = destination
    private var gridDetails = gridDetails

    private var comparator: Comparator<ArenaCell> = ArenaCellComparator

    private val frontier: PriorityQueue<ArenaCell> = PriorityQueue<ArenaCell>(1000, comparator)
    private var costSoFar: HashMap<String, Int> = HashMap()

    private var currentCell: ArenaCell = gridDetails.getArenaCell(gridDetails.getRobotCenterRow(), gridDetails.getRobotCenterCol())
    private var nextNodes: ArrayList<ArenaCell> = ArrayList<ArenaCell>()

    private var previousCells: HashMap<String, String> = HashMap()
    private var path: Stack<ArenaCell> = Stack<ArenaCell>()

    fun getFastestPath(): Stack<ArenaCell> {
        Log.v("SR", "Finding Fastest Path. (4th ver)")

        // Get row and col information for destination
        val targetRow = destination.getIndexRow()
        val targetCol = destination.getIndexColumn()
        val targetOrientation = destination.getRobotDirection()
        var counter = 0

        // Add starting position to frontier
        currentCell = gridDetails.getArenaCell(gridDetails.getRobotCenterCol(), gridDetails.getRobotCenterRow())
        currentCell.setActualCostSoFar(0)
        currentCell.setRobotDirection(gridDetails.getRobotDirection())
        val startingCellKey = currentCell.getIndexColumn().toString() + "," + currentCell.getIndexRow().toString() + "," +  currentCell.getRobotDirection().toString()
        costSoFar.put(startingCellKey, 0)
        previousCells[startingCellKey] = ""
        //currentCell.setHeuristicCost(calculateHeuristicCost(currentCell))
        frontier.add(currentCell)

        while (frontier.isNotEmpty()){

            counter++
            // Remove ArenaCell with least fn
            currentCell = frontier.poll()

            var currentCellKey = currentCell.getIndexColumn().toString() + "," + currentCell.getIndexRow().toString() + "," + currentCell.getRobotDirection().toString()
            Log.v("SR", "expanding: $currentCellKey")

            // If ArenaCell to expand is goal state, we are done, retrieve path to this
            if (currentCell.getIndexRow() == targetRow && currentCell.getIndexColumn() == targetCol && currentCell.getRobotDirection() == targetOrientation){
                Log.v("SR", "path found!")
                path = getPath(currentCellKey)
                gridDetails.setRobotDirection(targetOrientation)
                return path
            }

            // Retrieve nodes reachable from this current node
            nextNodes = getValidNeighbours(currentCell)

            for (nextCell in nextNodes){

                //Log.v("SR", "Checking nextCell in nextNodes ")

                var nextCellKey = nextCell.getIndexColumn().toString() + "," + nextCell.getIndexRow().toString() + "," + nextCell.getRobotDirection().toString()
                Log.v("SR", nextCellKey)
                Log.v("SR", costSoFar["4,5,EAST"].toString())

                // Get actual cost to this neighbour cell through the current cell
                val newCost: Int = if (costSoFar[currentCellKey] != null){
                    getMovementCost(currentCell, nextCell) + costSoFar[currentCellKey]!!
                } else {
                    getMovementCost(currentCell, nextCell)
                }

                //Log.v("SR", "Before condition check - cost so far: ${costSoFar[nextCellKey]} and newCost: $newCost")

                if (!costSoFar.containsKey(nextCellKey) || newCost < (costSoFar[nextCellKey]!!)){
                    Log.v("SR", "Adding node to frontier: x:${nextCell.getIndexColumn()}, y: ${nextCell.getIndexRow()}, direction: ${nextCell.getRobotDirection()}")
                    nextCell.setActualCostSoFar(newCost)
                    nextCell.setHeuristicCost(calculateHeuristicCost(nextCell))
                    costSoFar.put(nextCellKey, newCost)
                    frontier.add(nextCell)
                    previousCells[nextCellKey] = currentCellKey
                }

            }
        }

        Log.v("SR", path.size.toString())
        Log.v("SR", "cells expanded = $counter")
        return path
    }

    //TODO: Check if additional checks needed (especially for turning)
    private fun checkIfCanVisit(cell: ArenaCell): Boolean{
        return !cell.isObstacle() && !cell.isVirtualObstacle() && !cell.isVirtualWall()
    }

    private fun getValidNeighbours(cell: ArenaCell): ArrayList<ArenaCell> {

        val cellRow = cell.getIndexRow()
        val cellCol = cell.getIndexColumn()
        val neighbours = ArrayList<ArenaCell>()
        val robotDirection = cell.getRobotDirection()

        Log.v("SR", "Finding neighbours for x: ${cellCol}, y: ${cellRow}, direction: $robotDirection")

        //TODO: do checkIfCanVisit for all movement in left/right turns
        if (robotDirection == Direction.NORTH){
            //turn left
            if (cellCol - 3 > -1 && cellRow + 2 < 20){
                val leftForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 3, cellRow + 2)
                leftForwardCell.setRobotDirection(Direction.WEST)
                if (checkIfCanVisit(leftForwardCell)){
                    Log.v("SR", "Left Forward x: ${leftForwardCell.getIndexColumn()}, y: ${leftForwardCell.getIndexRow()}, direction: ${leftForwardCell.getRobotDirection()}")
                    neighbours.add(leftForwardCell)
                }
            }
            //turn right
            if (cellCol + 3 < 20 && cellRow + 2 < 20){
                val rightForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 3, cellRow + 2)
                rightForwardCell.setRobotDirection(Direction.EAST)
                if (checkIfCanVisit(rightForwardCell)){
                    Log.v("SR", "Right Forward x: ${rightForwardCell.getIndexColumn()}, y: ${rightForwardCell.getIndexRow()}, direction: ${rightForwardCell.getRobotDirection()}")
                    neighbours.add(rightForwardCell)
                }
            }
            //move forward
            if (cellRow + 1 < 20){
                val upCell: ArenaCell = gridDetails.getArenaCell(cellCol, cellRow + 1)
                upCell.setRobotDirection(Direction.NORTH)
                if (checkIfCanVisit(upCell)){
                    Log.v("SR", "Up x: ${upCell.getIndexColumn()}, y: ${upCell.getIndexRow()}, direction: ${upCell.getRobotDirection()}")
                    neighbours.add(upCell)
                }
            }
            //reverse
            if (cellRow - 1 > -1){
                val downCell: ArenaCell = gridDetails.getArenaCell(cellCol, cellRow - 1)
                downCell.setRobotDirection(Direction.NORTH)
                if (checkIfCanVisit(downCell)){
                    Log.v("SR", "Down x: ${downCell.getIndexColumn()}, y: ${downCell.getIndexRow()}, direction: ${downCell.getRobotDirection()}")
                    neighbours.add(downCell)
                }
            }
            //back right
            if (cellRow - 4 > -1 && cellCol + 2 < 20){
                val rightBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 2, cellRow - 4)
                rightBackwardCell.setRobotDirection(Direction.WEST)
                if (checkIfCanVisit(rightBackwardCell)){
                    Log.v("SR", "Right Backward x: ${rightBackwardCell.getIndexColumn()}, y: ${rightBackwardCell.getIndexRow()}, direction: ${rightBackwardCell.getRobotDirection()}")
                    neighbours.add(rightBackwardCell)
                }
            }
            //back left
            if (cellRow - 4 > -1 && cellCol - 2 > -1){
                val leftBackwardCell: ArenaCell = gridDetails.getArenaCell( cellCol - 2, cellRow - 4)
                leftBackwardCell.setRobotDirection(Direction.EAST)
                if (checkIfCanVisit(leftBackwardCell)){
                    Log.v("SR", "Left Backward x: ${leftBackwardCell.getIndexColumn()}, y: ${leftBackwardCell.getIndexRow()}, direction: ${leftBackwardCell.getRobotDirection()}")
                    neighbours.add(leftBackwardCell)
                }
            }
        }

        else if (robotDirection == Direction.SOUTH){
            if (cellCol + 3 < 20 && cellRow - 2 > -1){
                val leftForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 3, cellRow - 2)
                leftForwardCell.setRobotDirection(Direction.EAST)
                if (checkIfCanVisit(leftForwardCell)){
                    Log.v("SR", "Left Forward x: ${leftForwardCell.getIndexColumn()}, y: ${leftForwardCell.getIndexRow()}, direction: ${leftForwardCell.getRobotDirection()}")
                    neighbours.add(leftForwardCell)
                }
            }
            if (cellCol - 3 > -1 && cellRow - 2 > -1){
                val rightForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 3, cellRow - 2)
                rightForwardCell.setRobotDirection(Direction.WEST)
                if (checkIfCanVisit(rightForwardCell)){
                    Log.v("SR", "Right Forward x: ${rightForwardCell.getIndexColumn()}, y: ${rightForwardCell.getIndexRow()}, direction: ${rightForwardCell.getRobotDirection()}")
                    neighbours.add(rightForwardCell)
                }
            }
            if (cellRow - 1 > -1){
                val upCell: ArenaCell = gridDetails.getArenaCell(cellCol, cellRow - 1)
                upCell.setRobotDirection(Direction.SOUTH)
                if (checkIfCanVisit(upCell)){
                    Log.v("SR", "Up x: ${upCell.getIndexColumn()}, y: ${upCell.getIndexRow()}, direction: ${upCell.getRobotDirection()}")
                    neighbours.add(upCell)
                }
            }
            if (cellRow + 1 < 20){
                val downCell: ArenaCell = gridDetails.getArenaCell(cellCol, cellRow + 1)
                downCell.setRobotDirection(Direction.SOUTH)
                if (checkIfCanVisit(downCell)){
                    Log.v("SR", "Down x: ${downCell.getIndexColumn()}, y: ${downCell.getIndexRow()}, direction: ${downCell.getRobotDirection()}")
                    neighbours.add(downCell)
                }
            }
            if (cellRow + 4 < 20 && cellCol - 2 > -1){
                val rightBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 2, cellRow + 4)
                rightBackwardCell.setRobotDirection(Direction.EAST)
                if (checkIfCanVisit(rightBackwardCell)){
                    Log.v("SR", "Right Backward x: ${rightBackwardCell.getIndexColumn()}, y: ${rightBackwardCell.getIndexRow()}, direction: ${rightBackwardCell.getRobotDirection()}")
                    neighbours.add(rightBackwardCell)
                }
            }
            if (cellRow + 4 < 20 && cellCol + 2 < 20){
                val leftBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 2, cellRow + 4)
                leftBackwardCell.setRobotDirection(Direction.WEST)
                if (checkIfCanVisit(leftBackwardCell)){
                    Log.v("SR", "Left Backward x: ${leftBackwardCell.getIndexColumn()}, y: ${leftBackwardCell.getIndexRow()}, direction: ${leftBackwardCell.getRobotDirection()}")
                    neighbours.add(leftBackwardCell)
                }
            }
        }

        else if (robotDirection == Direction.WEST){
            if (cellCol - 2 > -1 && cellRow - 3 > -1){
                val leftForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 2, cellRow - 3)
                leftForwardCell.setRobotDirection(Direction.SOUTH)
                if (checkIfCanVisit(leftForwardCell)){
                    Log.v("SR", "Left Forward x: ${leftForwardCell.getIndexColumn()}, y: ${leftForwardCell.getIndexRow()}, direction: ${leftForwardCell.getRobotDirection()}")
                    neighbours.add(leftForwardCell)
                }
            }
            if (cellCol - 2 > -1 && cellRow + 3 < 20){
                val rightForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 2, cellRow + 3)
                rightForwardCell.setRobotDirection(Direction.NORTH)
                if (checkIfCanVisit(rightForwardCell)){
                    Log.v("SR", "Right Forward x: ${rightForwardCell.getIndexColumn()}, y: ${rightForwardCell.getIndexRow()}, direction: ${rightForwardCell.getRobotDirection()}")
                    neighbours.add(rightForwardCell)
                }
            }
            if (cellCol - 1 > -1){
                val upCell: ArenaCell = gridDetails.getArenaCell(cellCol - 1, cellRow)
                upCell.setRobotDirection(Direction.WEST)
                if (checkIfCanVisit(upCell)){
                    Log.v("SR", "Up x: ${upCell.getIndexColumn()}, y: ${upCell.getIndexRow()}, direction: ${upCell.getRobotDirection()}")
                    neighbours.add(upCell)
                }
            }
            if (cellCol + 1 < 20){
                val downCell: ArenaCell = gridDetails.getArenaCell(cellCol + 1, cellRow)
                downCell.setRobotDirection(Direction.WEST)
                if (checkIfCanVisit(downCell)){
                    Log.v("SR", "Down x: ${downCell.getIndexColumn()}, y: ${downCell.getIndexRow()}, direction: ${downCell.getRobotDirection()}")
                    neighbours.add(downCell)
                }
            }
            if (cellRow + 2 < 20 && cellCol + 4 < 20){
                val rightBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 4, cellRow + 2)
                rightBackwardCell.setRobotDirection(Direction.SOUTH)
                if (checkIfCanVisit(rightBackwardCell)){
                    Log.v("SR", "Right Backward x: ${rightBackwardCell.getIndexColumn()}, y: ${rightBackwardCell.getIndexRow()}, direction: ${rightBackwardCell.getRobotDirection()}")
                    neighbours.add(rightBackwardCell)
                }
            }
            if (cellRow - 2 > -1 && cellCol + 4 < 20){
                val leftBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 4, cellRow - 2)
                leftBackwardCell.setRobotDirection(Direction.NORTH)
                if (checkIfCanVisit(leftBackwardCell)){
                    Log.v("SR", "Left Backward x: ${leftBackwardCell.getIndexColumn()}, y: ${leftBackwardCell.getIndexRow()}, direction: ${leftBackwardCell.getRobotDirection()}")
                    neighbours.add(leftBackwardCell)
                }
            }
        }

        else if (robotDirection == Direction.EAST){
            if (cellCol + 2 < 20 && cellRow + 3 < 20){
                val leftForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 2, cellRow + 3)
                leftForwardCell.setRobotDirection(Direction.NORTH)
                if (checkIfCanVisit(leftForwardCell)){
                    Log.v("SR", "Left Forward x: ${leftForwardCell.getIndexColumn()}, y: ${leftForwardCell.getIndexRow()}, direction: ${leftForwardCell.getRobotDirection()}")
                    neighbours.add(leftForwardCell)
                }
            }
            if (cellCol + 2 < 20 && cellRow - 3 > -1){
                val rightForwardCell: ArenaCell = gridDetails.getArenaCell(cellCol + 2, cellRow - 3)
                rightForwardCell.setRobotDirection(Direction.SOUTH)
                if (checkIfCanVisit(rightForwardCell)){
                    Log.v("SR", "Right Forward x: ${rightForwardCell.getIndexColumn()}, y: ${rightForwardCell.getIndexRow()}, direction: ${rightForwardCell.getRobotDirection()}")
                    neighbours.add(rightForwardCell)
                }
            }
            if (cellCol + 1 < 20){
                val upCell: ArenaCell = gridDetails.getArenaCell(cellCol + 1, cellRow)
                upCell.setRobotDirection(Direction.EAST)
                if (checkIfCanVisit(upCell)){
                    Log.v("SR", "Up x: ${upCell.getIndexColumn()}, y: ${upCell.getIndexRow()}, direction: ${upCell.getRobotDirection()}")
                    neighbours.add(upCell)
                }
            }
            if (cellCol - 1 > -1){
                val downCell: ArenaCell = gridDetails.getArenaCell(cellCol - 1, cellRow)
                downCell.setRobotDirection(Direction.EAST)
                if (checkIfCanVisit(downCell)){
                    Log.v("SR", "Down x: ${downCell.getIndexColumn()}, y: ${downCell.getIndexRow()}, direction: ${downCell.getRobotDirection()}")
                    neighbours.add(downCell)
                }
            }
            if (cellRow - 2 > -1 && cellCol - 4 > -1){
                val rightBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 4, cellRow - 2)
                rightBackwardCell.setRobotDirection(Direction.NORTH)
                if (checkIfCanVisit(rightBackwardCell)){
                    Log.v("SR", "Right Backward x: ${rightBackwardCell.getIndexColumn()}, y: ${rightBackwardCell.getIndexRow()}, direction: ${rightBackwardCell.getRobotDirection()}")
                    neighbours.add(rightBackwardCell)
                }
            }
            if (cellRow + 2 < 20 && cellCol - 4 > -1){
                val leftBackwardCell: ArenaCell = gridDetails.getArenaCell(cellCol - 4, cellRow + 2)
                leftBackwardCell.setRobotDirection(Direction.SOUTH)
                if (checkIfCanVisit(leftBackwardCell)){
                    Log.v("SR", "Left Backward x: ${leftBackwardCell.getIndexColumn()}, y: ${leftBackwardCell.getIndexRow()}, direction: ${leftBackwardCell.getRobotDirection()}")
                    neighbours.add(leftBackwardCell)
                }
            }
        }

        Log.v("SR", "---")
        return neighbours
    }

    private fun getMovementCost(currentCell: ArenaCell, newCell: ArenaCell): Int{

        val currentDirection = currentCell.getRobotDirection()
        val newDirection = newCell.getRobotDirection()

        if (currentDirection == Direction.NORTH){
            if (newDirection == Direction.WEST || newDirection == Direction.EAST){
                return 3
            }
        }
        else if (currentDirection == Direction.WEST){
            if (newDirection == Direction.SOUTH || newDirection == Direction.NORTH){
                return 3
            }
        }
        else if (currentDirection == Direction.EAST){
            if (newDirection == Direction.SOUTH || newDirection == Direction.NORTH){
                return 3
            }
        }
        else if (currentDirection == Direction.SOUTH){
            if (newDirection == Direction.WEST || newDirection == Direction.EAST){
                return 3
            }
        }
        return 1
    }

    private fun calculateHeuristicCost(currentCell: ArenaCell): Int{
        return abs(currentCell.getIndexRow() - destination.getIndexRow()) +
                abs(currentCell.getIndexColumn() - destination.getIndexColumn())
    }

    // Returns a stack of ArenaCells from the start to the end as a path
    private fun getPath(currentCellKey: String): Stack<ArenaCell> {

        val path = Stack<ArenaCell>()
        var pathNode : String = currentCellKey

        while (true){
            path.push(keyToCell(pathNode))
            //Log.v("path", "${pathNode?.getIndexRow()},${pathNode?.getIndexColumn()}" )
            pathNode = previousCells[pathNode].toString()
            Log.v("path", "next path is$pathNode")
            if (pathNode == ""){
                break
            }
        }

        return path
    }

    private fun keyToCell(key: String): ArenaCell {

        Log.v("tag", key)

        val stringSplit: List<String> = key.split(",")
        val cellColumn = Integer.parseInt(stringSplit[0])
        val cellRow = Integer.parseInt(stringSplit[1])
        val robotDirection = enumValueOf<Direction>(stringSplit[2])

        return ArenaCell(cellColumn, cellRow, robotDirection)
    }
}