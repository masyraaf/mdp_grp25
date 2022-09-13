package com.group6.mdpandroid.algorithm

class ArenaCell(indexColumnInput: Int, indexRowInput: Int, robotDirectionInput: Direction) {

    private var indexRow = indexRowInput
    private var indexColumn = indexColumnInput
    private var isObstacle = false
    private var isVirtualObstacle = false
    private var isVirtualWall = false
    private var imageDirection = Direction.NA
    private var robotDirection = robotDirectionInput
    private var actualCostSoFar = 0
    private var heuristicCost = 0

    fun getIndexRow(): Int {
        return indexRow
    }

    fun getIndexColumn(): Int {
        return indexColumn
    }

    fun isObstacle(): Boolean {
        return isObstacle
    }

    fun setIsObstacle(b: Boolean) {
        isObstacle = b
    }

    fun isVirtualObstacle(): Boolean {
        return isVirtualObstacle
    }

    fun setVirtualObstacle(b: Boolean) {
        isVirtualObstacle = b
    }

    fun isVirtualWall(): Boolean {
        return isVirtualWall
    }

    fun setVirtualWall(b: Boolean) {
        isVirtualWall = b
    }

    fun getImageDirection(): Direction {
        return imageDirection
    }

    fun setImageDirection(d: Direction) {
        imageDirection = d
    }

    fun getRobotDirection(): Direction {
        return robotDirection
    }

    fun setRobotDirection(d: Direction) {
        robotDirection = d
    }

    fun getActualCostSoFar(): Int {
        return actualCostSoFar
    }

    fun setActualCostSoFar(i: Int) {
        actualCostSoFar = i
    }

    fun getHeuristicCost(): Int {
        return heuristicCost
    }

    fun setHeuristicCost(i: Int) {
        heuristicCost = i
    }

    fun getfn(): Int{
        return actualCostSoFar + heuristicCost
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is ArenaCell -> {
                this.indexRow == other.indexRow &&
                        this.indexColumn == other.indexColumn &&
                        this.robotDirection == other.robotDirection
            }
            else -> false
        }

    }

}

enum class Direction {
    NORTH, SOUTH, EAST, WEST, NA
}