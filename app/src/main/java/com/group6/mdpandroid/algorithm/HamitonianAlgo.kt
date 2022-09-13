package com.group6.mdpandroid.algorithm

import android.util.Log
import java.util.*

class HamiltonianAlgo(gridDetails: GridDetails) {

    private var gridDetails = gridDetails

//    fun runImageRecognitionTask(): Pair<Stack<ArenaCell>, Queue<ArenaCell>> {
//
//        Log.v("SR", "Image recognition task started.")
//        val imageCellsToTake = gridDetails.getAllUntakenImageCells()
//        var robotCell: ArenaCell
//        val path = Stack<ArenaCell>()
//        val orderedObstacles: Queue<ArenaCell> = LinkedList()
//
//        while (imageCellsToTake.size > 0) {
//
//            Log.v("SR", "Remaining obstacle with image cells: ")
//            for (arenaCell in imageCellsToTake) {
//                Log.v(
//                    "SR",
//                    "Obstacle with image at: ${arenaCell.getIndexRow()}, ${arenaCell.getIndexColumn()}"
//                )
//            }
//
//            // Choose closest image from robot's current location and find the shortest path towards it
//            robotCell = gridDetails.getArenaCell(
//                gridDetails.getRobotCenterRow(),
//                gridDetails.getRobotCenterCol()
//            )
//            var sourceRow = robotCell.getIndexRow()
//            var sourceColumn = robotCell.getIndexColumn()
//
//            var comparator: Comparator<ArenaCell> =
//                ManhattanDistanceComparator(sourceRow, sourceColumn)
//            var imageCellsQueue = PriorityQueue(300, comparator)
//
//            for (arenaCell in imageCellsToTake) {
//                imageCellsQueue.add(arenaCell)
//            }
//
//            // Retrieve destination cell to reach and add to orderedObstacles
//            val nextImageCell = imageCellsQueue.poll()
//            orderedObstacles.add(nextImageCell)
//
//            Log.v(
//                "SR",
//                "Next obstacle with image to traverse to: ${nextImageCell.getIndexRow()}, ${nextImageCell.getIndexColumn()}"
//            )
//
//            val nextDestination = getDestination(nextImageCell)
//            Log.v(
//                "SR",
//                "Position to traverse to capture image: ${nextDestination.getIndexRow()}, ${nextDestination.getIndexColumn()}"
//            )
//
//            // Find a fastest path
//            val pathfinder = FastestPath2(gridDetails, nextDestination)
//            val pathFound = pathfinder.getFastestPath()
//            if (pathFound.isNotEmpty()){
//                path.addAll(0, pathFound)
//
//                Log.v("SR", "Path finding to obstacle successful.")
//                Log.v(
//                    "SR",
//                    "Path finding successful, with ${path.size} cells found for path"
//                )
//
//                // Update robot's position to destination reached
//                gridDetails.setRobotCenterRow(nextDestination.getIndexRow())
//                gridDetails.setRobotCenterCol(nextDestination.getIndexColumn())
//
//            }
//            else {
//                Log.v("SR", "No path found, skipping obstacle.")
//                orderedObstacles.remove(orderedObstacles.elementAt(orderedObstacles.size - 1))
//            }
//
//            Log.v("SR", "Path finding to obstacle successful.")
//            Log.v(
//                "SR",
//                "Path finding successful, with ${path.size} cells found for path, sequence is as follows:"
//            )
////            while (path.size > 0) {
////                var cell: ArenaCell = path.pop()
////                Log.v("SR", "${cell.getIndexRow()}, ${cell.getIndexColumn()}")
////            }
//
//            // Update robot's position to destination reached
//            gridDetails.setRobotCenterRow(nextDestination.getIndexRow())
//            gridDetails.setRobotCenterCol(nextDestination.getIndexColumn())
//
//            // Remove seen image from list of unseen images
//            imageCellsToTake.clear()
//            while (!imageCellsQueue.isEmpty()) {
//                imageCellsToTake.add(imageCellsQueue.poll())
//            }
//        }
//        // return path to gridMap fragment for update
//        return Pair(path, orderedObstacles)
//    }

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
                    "Obstacle with image at: ${arenaCell.getIndexRow()}, ${arenaCell.getIndexColumn()}"
                )
            }

            // Choose closest image from robot's current location and find the shortest path towards it
            robotCell = gridDetails.getArenaCell(
                gridDetails.getRobotCenterRow(),
                gridDetails.getRobotCenterCol()
            )
            var sourceRow = robotCell.getIndexRow()
            var sourceColumn = robotCell.getIndexColumn()

            var comparator: Comparator<Pair<ArenaCell, ArenaCell>> =
                ManhattanDistanceComparator(sourceRow, sourceColumn)
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
                "Next obstacle with image to traverse to: ${nextPair.first.getIndexRow()}, ${nextPair.first.getIndexColumn()}"
            )
            Log.v(
                "SR",
                "Position to traverse to capture image: ${nextPair.second.getIndexRow()}, ${nextPair.second.getIndexColumn()}"
            )

            // Find a fastest path
            val pathfinder = FastestPath3(gridDetails, nextPair.second)
            val pathFound = pathfinder.getFastestPath()
            if (pathFound.isNotEmpty()){
                path.addAll(0, pathFound)

                Log.v("SR", "Path finding to obstacle successful.")
                Log.v(
                    "SR",
                    "Path finding successful, with ${path.size} cells found for path"
                )

                // Update robot's position to destination reached
                gridDetails.setRobotCenterRow(nextPair.second.getIndexRow())
                gridDetails.setRobotCenterCol(nextPair.second.getIndexColumn())
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

    // Based off the image location and orientation, return position for robot to be in
    private fun getDestination(imageCell: ArenaCell): ArenaCell {

        return when {
            imageCell.getImageDirection() == Direction.NORTH -> {
                ArenaCell(imageCell.getIndexRow() + 3, imageCell.getIndexColumn(), Direction.WEST)
            }
            imageCell.getImageDirection() == Direction.EAST -> {
                ArenaCell(imageCell.getIndexRow(), imageCell.getIndexColumn() + 3, Direction.NORTH)
            }
            imageCell.getImageDirection() == Direction.WEST -> {
                ArenaCell(imageCell.getIndexRow(), imageCell.getIndexColumn() - 3, Direction.SOUTH)
            }
            else -> {
                ArenaCell(imageCell.getIndexRow() - 3, imageCell.getIndexColumn(), Direction.EAST)
            }
        }
    }
}