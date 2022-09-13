package com.group6.mdpandroid.entity

import com.group6.mdpandroid.utils.Constants


class GridPoint(
    var value: Constants.Companion.GridPointType = Constants.Companion.GridPointType.EMPTY,
    var xPos: Int = 0,
    var yPos: Int = 0,
    var textInside: Int? = 0
) {

    init {

    }
}