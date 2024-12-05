package com.exozet.sequentialimageplayer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ZoomageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : com.jsibbold.zoomage.ZoomageView(context, attrs, defStyleAttr) {

    internal var view: View? = null

    override fun onTouchEvent(event: MotionEvent): Boolean = when {
        event.pointerCount <= 1 -> {
            view?.onTouchEvent(event)
            setTranslatable(false) // disable translation while we have only one finger on screen
            super.onTouchEvent(event)
        }

        else -> {
            setTranslatable(true)
            super.onTouchEvent(event)
        }
    }
}