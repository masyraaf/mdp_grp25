package com.group6.mdpandroid.algorithm

import java.util.Comparator
import java.util.HashMap
import kotlin.math.abs

class ManhattanDistanceComparator2(private val sourceRow: Int, private val sourceCol: Int) : Comparator<Pair<ArenaCell, Pair<ArenaCell, ArenaCell?>>> {

    private fun getManhattanDistanceFromSource(arenaCell: ArenaCell): Int {
        val row = arenaCell.getIndexRow()
        val col = arenaCell.getIndexColumn()
        return abs(row - sourceRow) + abs(col - sourceCol)
    }

    override fun compare(arenaCell1: Pair<ArenaCell, Pair<ArenaCell, ArenaCell?>>, arenaCell2: Pair<ArenaCell, Pair<ArenaCell, ArenaCell?>>): Int {
        return if (getManhattanDistanceFromSource(arenaCell1.second.first) < getManhattanDistanceFromSource(
                arenaCell2.second.first
            )
        ) -1 else if (getManhattanDistanceFromSource(arenaCell1.second.first) > getManhattanDistanceFromSource(
                arenaCell2.second.first
            )
        ) 1 else {
            if (arenaCell1.second.first.getIndexRow() > sourceRow && arenaCell2.second.first.getIndexColumn() > sourceRow) {
                // choose the cell to the right
                if (arenaCell1.second.first.getIndexColumn() > arenaCell2.second.first.getIndexColumn()) {
                    -1
                } else {
                    0
                }
            } else if (arenaCell1.second.first.getIndexColumn() > sourceCol && arenaCell1.second.first.getIndexColumn() > sourceCol) {
                // choose the cell below
                if (arenaCell1.second.first.getIndexRow() < arenaCell2.second.first.getIndexRow()) {
                    -1
                } else {
                    0
                }
            } else if (arenaCell1.second.first.getIndexRow() < sourceRow && arenaCell2.second.first.getIndexColumn() < sourceRow) {
                // choose the cell to the left
                if (arenaCell1.second.first.getIndexColumn() < arenaCell2.second.first.getIndexColumn()) {
                    -1
                } else {
                    0
                }
            } else if (arenaCell1.second.first.getIndexColumn() < sourceCol && arenaCell1.second.first.getIndexColumn() < sourceCol) {
                // choose the cell above
                if (arenaCell1.second.first.getIndexRow() > arenaCell2.second.first.getIndexRow()) {
                    -1
                } else {
                    0
                }
            } else {
                0
            }
        }
    }

//    private fun getManhattanDistanceFromSource(arenaCell: ArenaCell): Int {
//        val row = arenaCell.getIndexRow()
//        val col = arenaCell.getIndexColumn()
//        return abs(row - sourceRow) + abs(col - sourceCol)
//    }
//
//    override fun compare(arenaCell1: ArenaCell, arenaCell2: ArenaCell): Int {
//        return if (getManhattanDistanceFromSource(arenaCell1) < getManhattanDistanceFromSource(
//                arenaCell2
//            )
//        ) -1 else if (getManhattanDistanceFromSource(arenaCell1) > getManhattanDistanceFromSource(
//                arenaCell2
//            )
//        ) 1 else {
//            if (arenaCell1.getIndexRow() > sourceRow && arenaCell2.getIndexColumn() > sourceRow) {
//                // choose the cell to the right
//                if (arenaCell1.getIndexColumn() > arenaCell2.getIndexColumn()) {
//                    -1
//                } else {
//                    0
//                }
//            } else if (arenaCell1.getIndexColumn() > sourceCol && arenaCell1.getIndexColumn() > sourceCol) {
//                // choose the cell below
//                if (arenaCell1.getIndexRow() < arenaCell2.getIndexRow()) {
//                    -1
//                } else {
//                    0
//                }
//            } else if (arenaCell1.getIndexRow() < sourceRow && arenaCell2.getIndexColumn() < sourceRow) {
//                // choose the cell to the left
//                if (arenaCell1.getIndexColumn() < arenaCell2.getIndexColumn()) {
//                    -1
//                } else {
//                    0
//                }
//            } else if (arenaCell1.getIndexColumn() < sourceCol && arenaCell1.getIndexColumn() < sourceCol) {
//                // choose the cell above
//                if (arenaCell1.getIndexRow() > arenaCell2.getIndexRow()) {
//                    -1
//                } else {
//                    0
//                }
//            } else {
//                0
//            }
//        }
//    }
}