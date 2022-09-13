package com.group6.mdpandroid.algorithm

import android.util.Log
import java.util.*

class HamiltonianAlgo3(gridDetails: GridDetails) {

    private var gridDetails = gridDetails

    fun runImageRecognitionTask(): Pair<Stack<ArenaCell>, Queue<ArenaCell>> {

        Log.v("SR", "Image recognition task started.")
        val imageCellsToTake = gridDetails.getAllUntakenImageCells()
        var robotCell: ArenaCell
        val path = Stack<ArenaCell>()
        val orderedObstacles: Queue<ArenaCell> = LinkedList()

        while (imageCellsToTake.size > 0) {

            Log.v("SR", "Remaining obstacle with image cells: ")
            for (arenaCell in imageCellsToTake) {
                Log.v(
                    "SR",
                    "Obstacle with image at: x: ${arenaCell.getIndexColumn()}, y: ${arenaCell.getIndexRow()}"
                )
            }

            // Choose closest image from robot's current location and find the shortest path towards it
            robotCell = gridDetails.getArenaCell(
                gridDetails.getRobotCenterCol(),
                gridDetails.getRobotCenterRow()
            )

            var sourceRow = robotCell.getIndexRow()
            var sourceColumn = robotCell.getIndexColumn()

            var comparator: Comparator<Pair<ArenaCell, Pair<ArenaCell,ArenaCell?>>> =
                ManhattanDistanceComparator2(sourceRow, sourceColumn)
            var destinationsPairQueue = PriorityQueue(300, comparator)

            // For each obstacle, find the intended destination, and add Pair(obstacle, destination)
            // comparator will sort based on distance from source cell and destination cell
            for (arenaCell in imageCellsToTake) {
                destinationsPairQueue.add(Pair(arenaCell, getDestination((arenaCell))))
            }

            // Retrieve destination cell to reach and add to orderedObstacles
            val nextPair = destinationsPairQueue.poll()
            orderedObstacles.add(nextPair.first)

            Log.v(
                "SR",
                "Next obstacle with image to traverse to: x:${nextPair.first.getIndexColumn()}, y:${nextPair.first.getIndexRow()}"
            )
            Log.v(
                "SR",
                "Position to traverse to capture image: x:${nextPair.second.first.getIndexColumn()}, y:${nextPair.second.first.getIndexRow()}, direction: ${nextPair.second.first.getRobotDirection()}} & " +
                        "x:${nextPair.second.second?.getIndexColumn()}, y:${nextPair.second.second?.getIndexRow()}, direction: ${nextPair.second.second?.getRobotDirection()}}"
            )

            // Find a fastest path
            val pathfinder = FastestPath7(gridDetails, nextPair.second)

            val pathFound = pathfinder.getFastestPath()
            if (pathFound.first.isNotEmpty()){
                path.addAll(0, pathFound.first)

                Log.v("SR", "Path finding to obstacle successful.")
                Log.v(
                    "SR",
                    "Path finding successful, with ${path.size} cells found for path"
                )

                // Update robot's position to destination reached
                gridDetails.setRobotCenterRow(pathFound.second.getIndexRow())
                gridDetails.setRobotCenterCol(pathFound.second.getIndexColumn())
            }
            else {
                Log.v("SR", "No path found, skipping obstacle.")
                orderedObstacles.remove(orderedObstacles.elementAt(orderedObstacles.size - 1))
            }


            // Remove seen image from list of unseen images
            imageCellsToTake.clear()
            while (!destinationsPairQueue.isEmpty()) {
                imageCellsToTake.add(destinationsPairQueue.poll().first)
            }
        }
        // return path to gridMap fragment for update
        return Pair(path, orderedObstacles)
    }

    private fun checkIfCanVisit(cell: ArenaCell): Boolean{
        return !cell.isObstacle() && !cell.isVirtualObstacle() && !cell.isVirtualWall()
    }


    // Based off the image location and orientation, return position for robot to be in
    private fun getDestination(imageCell: ArenaCell): Pair<ArenaCell, ArenaCell?> {

        val imageCellX = imageCell.getIndexColumn()
        val imageCellY = imageCell.getIndexRow()
        val imageCellDirection = imageCell.getImageDirection()

        if (imageCellX == 0){
            if (imageCellDirection == Direction.NORTH){
                return Pair(ArenaCell(1, imageCellY + 3, Direction.SOUTH), null)
            }
            else if (imageCellDirection == Direction.SOUTH){
                return Pair(ArenaCell(1, imageCellY - 3, Direction.NORTH), null)
            }
        }

        if (imageCellX == 19){
            if (imageCellDirection == Direction.NORTH){
                return Pair(ArenaCell(18, imageCellY + 3, Direction.SOUTH), null)
            }
            else if (imageCellDirection == Direction.SOUTH){
                return Pair(ArenaCell(18, imageCellY - 3, Direction.NORTH), null)
            }
        }

        if (imageCellY == 0){
            if (imageCellDirection == Direction.WEST){
                return Pair(ArenaCell(imageCellX - 3, 1, Direction.EAST), null)
            }
            else if (imageCellDirection == Direction.EAST){
                return Pair(ArenaCell(imageCellX + 3, 1, Direction.WEST), null)
            }
        }

        if (imageCellY == 19){
            if (imageCellDirection == Direction.WEST){
                return Pair(ArenaCell(imageCellX - 3, 18, Direction.EAST), null)
            }
            else if (imageCellDirection == Direction.EAST){
                return Pair(ArenaCell(imageCellX + 3, 18, Direction.WEST), null)
            }
        }

        return when {
            imageCell.getImageDirection() == Direction.NORTH -> {
                if (!checkIfCanVisit(gridDetails.getArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() + 5))){
                    Pair(ArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() + 3, Direction.SOUTH),
                        null)
                }
                else {
                    Pair(ArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() + 3, Direction.SOUTH),
                        ArenaCell(imageCell.getIndexColumn(),imageCell.getIndexRow() + 4, Direction.SOUTH))
                }
            }
            imageCell.getImageDirection() == Direction.EAST -> {
                if (!checkIfCanVisit(gridDetails.getArenaCell(imageCell.getIndexColumn() + 5, imageCell.getIndexRow()))) {
                    Pair(ArenaCell( imageCell.getIndexColumn() + 3, imageCell.getIndexRow(), Direction.WEST),
                        null)
                }
                else {
                    Pair(ArenaCell( imageCell.getIndexColumn() + 3, imageCell.getIndexRow(), Direction.WEST),
                        ArenaCell(imageCell.getIndexColumn() + 4, imageCell.getIndexRow(), Direction.WEST))
                }

            }
            imageCell.getImageDirection() == Direction.WEST -> {
                if (!checkIfCanVisit(gridDetails.getArenaCell(imageCell.getIndexColumn() - 5, imageCell.getIndexRow()))) {
                    Pair(ArenaCell(imageCell.getIndexColumn() - 3, imageCell.getIndexRow(), Direction.EAST),
                        null)
                }
                else {
                    Pair(ArenaCell(imageCell.getIndexColumn() - 3, imageCell.getIndexRow(), Direction.EAST),
                        ArenaCell(imageCell.getIndexColumn() - 4, imageCell.getIndexRow(), Direction.EAST))
                }

            }
            else -> {
                if (!checkIfCanVisit(gridDetails.getArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() - 5))) {
                    Pair(ArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() - 3, Direction.NORTH),
                        null)
                }
                else {
                    Pair(ArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() - 3, Direction.NORTH),
                        ArenaCell(imageCell.getIndexColumn(), imageCell.getIndexRow() - 4, Direction.NORTH))
                }
            }
        }
    }
}