package com.group6.mdpandroid.utils

class CommandGenerator {
    companion object {

        fun generateCommand(direction: String, distance: String?, angle: String?, index: String?, leftRight: String?): String {
            var result: String = direction
            when (direction) {


                // UP CMD
                Constants.upCmd -> {
                    if (distance == null) {
                        result += Constants.DEFAULT_DISTANCE
                    } else {
                        result += distance
                    }
                }
                Constants.downCmd -> {
                    if (distance == null) {
                        result += Constants.DEFAULT_DISTANCE
                    } else {
                        result += distance
                    }
                }

                // ANGLE
                Constants.upLeftCmd -> {
                    if (angle == null) {
                        result += Constants.DEFAULT_ANGLE
                    } else {
                        result += angle
                    }
                }
                Constants.upRightCmd -> {
                    if (angle == null) {
                        result += Constants.DEFAULT_ANGLE
                    } else {
                        result += angle
                    }
                }
                Constants.onTheSpotLeft -> {
                    if (angle == null) {
                        result += Constants.DEFAULT_ANGLE
                    } else {
                        result += angle
                    }
                }
                Constants.onTheSpotRight -> {
                    if (angle == null) {
                        result += Constants.DEFAULT_ANGLE
                    } else {
                        result += angle
                    }
                }
                Constants.downLeftCmd -> {
                    if (angle == null) {
                        result += Constants.DEFAULT_ANGLE
                    } else {
                        result += angle
                    }
                }
                Constants.downRightCmd -> {
                    if (angle == null) {
                        result += Constants.DEFAULT_ANGLE
                    } else {
                        result += angle
                    }
                }

                // take pic
                Constants.takePic -> {
                    result = Constants.takePic + index + leftRight
                }
            }
            return result.padEnd(5, 'X')+"|"
        }
    }
}