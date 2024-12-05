package com.exozet.sequentialimageplayer.glrippleview

import android.view.View.MeasureSpec
import kotlin.math.min

/**
 * This class is a helper to measure views that require a specific aspect ratio.<br></br>
 * <br></br>
 * The measurement calculation is differing depending on whether the height and width
 * are fixed (match_parent or a dimension) or not (wrap_content)
 *
 * <pre>
 * | Width fixed | Width dynamic |
 * ---------------+-------------+---------------|
 * Height fixed   |      1      |       2       |
 * ---------------+-------------+---------------|
 * Height dynamic |      3      |       4       |
</pre> *
 * Everything is measured according to a specific aspect ratio.<br></br>
 * <br></br>
 *
 *  * 1: Both width and height fixed:   Fixed (Aspect ratio isn't respected)
 *  * 2: Width dynamic, height fixed:   Set width depending on height
 *  * 3: Width fixed, height dynamic:   Set height depending on width
 *  * 4: Both width and height dynamic: Largest size possible
 *
 *
 * @author Jesper Borgstrup
 */
class ViewAspectRatioMeasurer(private val aspectRatio: Double) {

    /**
     * Measure with a specific aspect ratio<br></br>
     * <br></br>
     * After measuring, get the width and height with the [.getMeasuredWidth]
     * and [.getMeasuredHeight] methods, respectively.
     *
     * @param widthMeasureSpec  The width <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
     * @param heightMeasureSpec The height <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
     * @param aspectRatio       The aspect ratio to calculate measurements in respect to
     */
    /**
     * Measure with the aspect ratio given at construction.<br></br>
     * <br></br>
     * After measuring, get the width and height with the [.getMeasuredWidth]
     * and [.getMeasuredHeight] methods, respectively.
     *
     * @param widthMeasureSpec  The width <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
     * @param heightMeasureSpec The height <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
     */
    @JvmOverloads
    fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        aspectRatio: Double = this.aspectRatio
    ) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize =
            if (widthMode == MeasureSpec.UNSPECIFIED) Int.Companion.MAX_VALUE else MeasureSpec.getSize(
                widthMeasureSpec
            )
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize =
            if (heightMode == MeasureSpec.UNSPECIFIED) Int.Companion.MAX_VALUE else MeasureSpec.getSize(
                heightMeasureSpec
            )

        if (heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY) {
            /*
             * Possibility 1: Both width and height fixed
             */
            measuredWidth = widthSize
            measuredHeight = heightSize
        } else if (heightMode == MeasureSpec.EXACTLY) {
            /*
             * Possibility 2: Width dynamic, height fixed
             */
            measuredHeight = min(heightSize.toDouble(), widthSize / aspectRatio) as Int
            measuredWidth = (measuredHeight!! * aspectRatio).toInt()
        } else if (widthMode == MeasureSpec.EXACTLY) {
            /*
             * Possibility 3: Width fixed, height dynamic
             */
            measuredWidth = min(widthSize.toDouble(), heightSize * aspectRatio) as Int
            measuredHeight = (measuredWidth!! / aspectRatio).toInt()
        } else {
            /*
             * Possibility 4: Both width and height dynamic
             */
            if (widthSize > heightSize * aspectRatio) {
                measuredHeight = heightSize
                measuredWidth = (measuredHeight!! * aspectRatio).toInt()
            } else {
                measuredWidth = widthSize
                measuredHeight = (measuredWidth!! / aspectRatio).toInt()
            }
        }
    }

    var measuredWidth: Int? = null

    /**
     * Get the width measured in the latest call to <tt>measure()</tt>.
     */
    fun getMeasuredWidth(): Int {
        checkNotNull(measuredWidth) { "You need to run measure() before trying to get measured dimensions" }
        return measuredWidth!!
    }

    var measuredHeight: Int? = null

    /**
     * Get the height measured in the latest call to <tt>measure()</tt>.
     */
    fun getMeasuredHeight(): Int {
        checkNotNull(measuredHeight) { "You need to run measure() before trying to get measured dimensions" }
        return measuredHeight!!
    }
}