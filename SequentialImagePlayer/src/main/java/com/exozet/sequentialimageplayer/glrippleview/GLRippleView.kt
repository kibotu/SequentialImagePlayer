package com.exozet.sequentialimageplayer.glrippleview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.exozet.sequentialimageplayer.R

/**
 * Created by Ryota Niinomi on 2017/05/24.
 */
class GLRippleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {

    companion object {
        val OPENGL_ES_VERSION = 2
    }

    private var bgImage: Bitmap? = null
    private var renderer: RippleRenderer

    var listener: Listener? = null

    init {
        setBackgroundImage(attrs)

        val image = bgImage?.run { mutableListOf(this) } ?: mutableListOf()

        renderer = RippleRenderer(context.applicationContext, image)

        setEGLContextClientVersion(OPENGL_ES_VERSION)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    /**
     * Add images to cross-fade.
     */
    fun addBackgroundImages(images: List<Bitmap>) {
        renderer.addBackgroundImages(images)
    }

    /**
     * Add images to cross-fade.
     */
    fun setBackground(image: Bitmap) {
        renderer.setBackground(image)
    }

    /**
     * Set duration for cross-fade.
     */
    fun setFadeDuration(duration: Long) {
        renderer.fadeDuration = duration
    }

    /**
     * Set interval time until start cross-fade.
     */
    fun setFadeInterval(interval: Long) {
        renderer.fadeInterval = interval
    }

    /**
     * Set center point of ripple.
     * @FloatRange(from = -1.0, to = 1.0)
     */
    fun setRipplePoint(xAndY: Pair<Float, Float>) {
        renderer.point = xAndY
    }

    /**
     * Set offset for ripple.
     * This value affects to ripple strength.
     */
    fun setRippleOffset(offset: Float) {
        renderer.rippleOffset = offset
    }

    /**
     * Start cross-fade animation.
     */
    fun startCrossFadeAnimation() {
        renderer.startCrossFadeAnimation()
    }


    fun setStrength(strength: Float) {
        renderer.strength = strength
    }

    fun setZoom(zoom: Float) {
        renderer.zoom = zoom
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        listener?.onTouchEvent(event)

        return true
    }

    private fun setBackgroundImage(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GLRippleView)

        typedArray.getDrawable(R.styleable.GLRippleView_backgroundImage)?.let { drawable ->
            bgImage = (drawable as BitmapDrawable).bitmap
            typedArray.recycle()
        }
    }

    interface Listener {
        fun onTouchEvent(event: MotionEvent)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (isInEditMode) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        Log.v(this.javaClass.simpleName, "onMeasure w ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.v(this.javaClass.simpleName, "onMeasure h ${MeasureSpec.toString(heightMeasureSpec)}")

        val varm = if (bgImage == null)
            ViewAspectRatioMeasurer(widthMeasureSpec / heightMeasureSpec.toDouble())
        else
            ViewAspectRatioMeasurer(bgImage!!.width / bgImage!!.height.toDouble())

        varm.measure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(varm.measuredWidth, varm.measuredHeight)
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }

        if (result < desiredSize) {
            Log.e(this.javaClass.simpleName, "The view is too small, the content might get cut")
        }
        return result
    }
}