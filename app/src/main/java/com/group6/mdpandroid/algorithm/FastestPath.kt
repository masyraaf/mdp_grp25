package com.group6.mdpandroid.algorithm

import android.util.Log
import com.group6.mdpandroid.utils.Constants
import java.util.*

class FastestPath(gridDetails: GridDetails) {

    private var gridDetails = gridDetails
    private var actualPathCost: Array<IntArray> = Array(20) { IntArray(20) }

    private var currentCell: ArenaCell = gridDetails.getArenaCell(gridDetails.getRobotCenterRow(), gridDetails.getRobotCenterCol())
    private lateinit var currentDirection: Direction
    private var nextNodes: ArrayList<ArenaCell> = ArrayList<ArenaCell>()

    private var frontier: ArrayList<ArenaCell> = ArrayList<ArenaCell>()
    private var visited: Array<BooleanArray> = Array(20) { BooleanArray(20) }
    private var previousCell: HashMap<ArenaCell, ArenaCell> = HashMap<ArenaCell, ArenaCell>()
    private lateinit var path: Stack<ArenaCell>


    init {
        frontier.add(currentCell)

        for (row in 0 until Constants.GRID_HEIGHT) {
            for (col in 0 until Constants.GRID_WIDTH) {
                visited[row][col] = false
                if (canVisit(gridDetails.getArenaCell(row, col))) {
                    actualPathCost[row][col] = 0
                } else {
                    actualPathCost[row][col] = Constants.INFINITE_COST
                }
            }
        }
    }

    private fun canVisit(c: ArenaCell): Boolean {
//        return !c.isObstacle() && !c.isVirtualWall() && !c.isVirtualObstacle() && c.isExplored()
        return !c.isObstacle() && !c.isVirtualObstacle() && !c.isVirtualWall()
    }

    fun run(destination: ArenaCell){
        Log.v("SR", "Finding fastest path")
        var targetRow = destination.getIndexRow()
        var targetCol = destination.getIndexColumn()


        do {
            if (visited[targetRow][targetCol]) {
                path = getPath(targetRow, targetCol);
                Log.v("SR", "Fastest path has been found.")
                navigate(path);
                return;
            }

            currentCell = getMinCost(targetRow, targetCol)

            visited[currentCell.getIndexRow()][currentCell.getIndexColumn()] = true
            frontier.remove(currentCell)
            Log.v("SR", "Frontier size is ${frontier.size}")


            // TODO: Retrieve all possible cells to move to from here and check if can visit
            // TODO: If so, add to nextNodes

            for (i in 0 until nextNodes.size) {
                val temp: ArenaCell = nextNodes[i]
                if (temp != null && !visited[temp.getIndexRow()][temp.getIndexColumn()]) {
                    if (!frontier.contains(temp)) {
                        actualPathCost[temp.getIndexRow()][temp.getIndexColumn()] =
                            actualPathCost[currentCell.getIndexRow()][currentCell.getIndexColumn()] +
                                    getActualPathCost(currentCell, temp, currentDirection)
                        frontier.add(temp)
                        previousCell[temp] = currentCell
                    } else {
                        val newScore: Int =
                            actualPathCost[currentCell.getIndexRow()][currentCell.getIndexColumn()] +
                                    getActualPathCost(currentCell, temp, currentDirection)
                        if (newScore < actualPathCost[temp.getIndexRow()][temp.getIndexColumn()]) {
                            actualPathCost[temp.getIndexRow()][temp.getIndexColumn()] = newScore
                            previousCell[temp] = currentCell
                        }
                    }
                }
            }

        } while (frontier.isNotEmpty())

        Log.v("SR", "No path found")
    }

    private fun getMinCost(targetRow: Int, targetCol: Int) : ArenaCell {
        var min = Constants.INFINITE_COST.toDouble()
        var answer: ArenaCell? = null

        for (i in frontier.indices.reversed()) {
            val gn = actualPathCost[frontier[i].getIndexRow()][frontier[i].getIndexColumn()].toDouble()
            val fn = gn + getHeuristicPathCost(frontier[i], targetRow, targetCol)
            if (fn < min) {
                min = fn
                answer = frontier[i]
            }
        }
        return answer!!
    }

    private fun getActualPathCost(cur: ArenaCell, target: ArenaCell, dir: Direction): Int {
        //TODO: Implement actual path cost
        return 0 //temp return 0 to prevent error lol for now
    }

    private fun getHeuristicPathCost(currentCell: ArenaCell, targetRow: Int, targetCol: Int): Int {
        //TODO: Implement actual heuristic path cost
        return 0 //temp return 0 to prevent error lol for now
    }

    // Returns a stack of ArenaCells from the start to the end as a path
    private fun getPath(targetRow: Int, targetCol: Int): Stack<ArenaCell> {

        var path = Stack<ArenaCell>()
        var currentCell = gridDetails.getArenaCell(targetRow, targetCol)

        while (true){
            path.push(currentCell)
            currentCell = previousCell.get(currentCell)!!
            if (previousCell.get(currentCell) == null){
                break
            }
        }

        return path
    }

    // Move the robot based on the path
    private fun navigate(path: Stack<ArenaCell>) {

        //TODO: Translate the path into commands for the robot

    }

}