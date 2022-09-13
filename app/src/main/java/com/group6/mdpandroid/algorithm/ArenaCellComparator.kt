package com.group6.mdpandroid.algorithm

class ArenaCellComparator {
    companion object : Comparator<ArenaCell> {
        override fun compare(a: ArenaCell, b: ArenaCell): Int {
            val result = a.getfn() - b.getfn()
            return when {
                result == 0 -> {
                    0
                }
                result > 0 -> {
                    1
                }
                else -> {
                    -1
                }
            }
        }
    }
}