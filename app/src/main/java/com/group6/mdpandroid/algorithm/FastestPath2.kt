package com.group6.mdpandroid.algorithm

import android.util.Log
import com.group6.mdpandroid.utils.Constants
import java.util.*
import kotlin.math.abs

class FastestPath2(gridDetails: GridDetails, destination: ArenaCell) {

    private var destination = destination
    private var gridDetails = gridDetails

    private var comparator: Comparator<ArenaCell> = ArenaCellComparator

    private val frontier: PriorityQueue<ArenaCell> = PriorityQueue<ArenaCell>(400, comparator)
    private var costSoFar: Dictionary<ArenaCell, Int> = Hashtable()

    private var currentCell: ArenaCell = gridDetails.getArenaCell(gridDetails.getRobotCenterRow(), gridDetails.getRobotCenterCol())
    private var nextNodes: ArrayList<ArenaCell> = ArrayList<ArenaCell>()

    private var previousCells: HashMap<ArenaCell, ArenaCell> = HashMap<ArenaCell, ArenaCell>()
    private var path: Stack<ArenaCell> = Stack<ArenaCell>()

    fun getFastestPath(): Stack<ArenaCell> {
        Log.v("SR", "Finding Fastest Path.")

        // Get row and col information for destination
        var targetRow = destination.getIndexRow()
        var targetCol = destination.getIndexColumn()

        // Add starting position to frontier
        currentCell = gridDetails.getArenaCell(gridDetails.getRobotCenterRow(), gridDetails.getRobotCenterCol())
        currentCell.setActualCostSoFar(0)
        costSoFar.put(currentCell, 0)
        //previousCells[currentCell] == null
        //currentCell.setHeuristicCost(calculateHeuristicCost(currentCell))
        frontier.add(currentCell)

        while (frontier.isNotEmpty()){

            // Remove ArenaCell with least fn
            currentCell = frontier.poll()

            // If ArenaCell to expand is goal state, we are done, retrieve path to this
            if (currentCell.getIndexRow() == targetRow && currentCell.getIndexColumn() == targetCol)
                path = getPath(currentCell)

            // Retrieve nodes reachable from this current node
            nextNodes = getValidNeighbours(currentCell)

            for (nextCell in nextNodes){

                // Get actual cost to this neighbour cell through the current cell
                val newCost = costSoFar[currentCell] + getMovementCost(currentCell, nextCell)

                // Case 1 - If neighbour cell has not been expanded:
                //   need to add it to frontier for pending exploration
                // Case 2 - If neighbour cell has been expanded already but the new cost is less:
                //   re-add it to frontier for re-exploration since paths through it need updating
                // Case 3 - If neighbour cell has been expanded already but the new cost is more:
                //   nothing needs to be done, the neighbour cell already has a shorter path towards it
                if (costSoFar[nextCell] == null || newCost < costSoFar[currentCell]){
                    nextCell.setActualCostSoFar(newCost)
                    nextCell.setHeuristicCost(calculateHeuristicCost(currentCell))
                    costSoFar.put(nextCell, newCost)
                    frontier.add(nextCell)
                    previousCells[nextCell] = currentCell
                }

            }
        }
        Log.v("SR", path.size.toString())
        return path
    }

    private fun checkIfCanVisit(cell: ArenaCell): Boolean{
        return !cell.isObstacle() && !cell.isVirtualObstacle() && !cell.isVirtualWall()
    }

    private fun getValidNeighbours(cell: ArenaCell): ArrayList<ArenaCell> {

        val cellRow = cell.getIndexRow()
        val cellCol = cell.getIndexColumn()
        val neighbours = ArrayList<ArenaCell>()

        //TODO: To change to actual possible cells reachable by real robot with turning
        if (cellCol - 1 > -1){
            var leftCell: ArenaCell = gridDetails.getArenaCell(cellRow, cellCol - 1)
            if (checkIfCanVisit(leftCell)){
                neighbours.add(leftCell)
            }
        }
        if (cellCol + 1 < 20){
            var rightCell: ArenaCell = gridDetails.getArenaCell(cellRow, cellCol + 1)
            if (checkIfCanVisit(rightCell)){
                neighbours.add(rightCell)
            }
        }
        if (cellRow + 1 < 20){
            var upCell: ArenaCell = gridDetails.getArenaCell(cellRow + 1, cellCol)
            if (checkIfCanVisit(upCell)){
                neighbours.add(upCell)
            }
        }
        if (cellRow - 1 > -1){
            var downCell: ArenaCell = gridDetails.getArenaCell(cellRow - 1, cellCol)
            if (checkIfCanVisit(downCell)){
                neighbours.add(downCell)
            }
        }
        return neighbours
    }

    private fun getMovementCost(currentCell: ArenaCell, newCell: ArenaCell): Int{
        //TODO: To change to actual movement costs with turning (i.e. turning is more expensive)
        return Constants.MOVE_ONE_BLOCK
    }

    private fun calculateHeuristicCost(currentCell: ArenaCell): Int{
        return abs(currentCell.getIndexRow() - destination.getIndexRow()) +
                abs(currentCell.getIndexColumn() - destination.getIndexColumn())
    }

    // Returns a stack of ArenaCells from the start to the end as a path
    private fun getPath(currentCell: ArenaCell): Stack<ArenaCell> {

        val path = Stack<ArenaCell>()
        var pathNode : ArenaCell? = currentCell

        while (true){
            path.push(pathNode)
            //Log.v("path", "${pathNode?.getIndexRow()},${pathNode?.getIndexColumn()}" )
            pathNode = previousCells[pathNode]
            if (pathNode == null){
                break
            }
        }

        return path
    }
}