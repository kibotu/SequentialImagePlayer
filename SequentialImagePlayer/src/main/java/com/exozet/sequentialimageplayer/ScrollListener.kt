package com.exozet.sequentialimageplayer

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

open class ScrollListener(
    val width: () -> Int,
    val height: () -> Int,
    val onScroll: (percentX: Float, percentY: Float) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    /**
     * Min Swipe X-Distance
     */
    var thresholdX = 3f

    /**
     * Min Swipe Y-Distance
     */
    var thresholdY = 3f

    /**
     * Starting X-Position
     */
    protected var startX = 0f

    /**
     * Starting Y-Position
     */
    protected var startY = 0f

    override fun onDown(e: MotionEvent): Boolean {
        startX = e.x
        startY = e.y
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {

        val dX = startX - e2.x
        val dY = startY - e2.y

        return when {
            abs(dX) <= thresholdX && abs(dY) <= thresholdY -> false
            else -> {

                val percentX = (dX) / width()
                val percentY = (dY) / height()

                onScroll(percentX, percentY)

                true
            }
        }
    }
}