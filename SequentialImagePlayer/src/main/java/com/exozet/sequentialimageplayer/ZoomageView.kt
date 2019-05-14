package com.exozet.sequentialimageplayer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class ZoomageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : com.jsibbold.zoomage.ZoomageView(context, attrs, defStyleAttr) {

    override fun onTouchEvent(event: MotionEvent?): Boolean = when {
        event?.pointerCount ?: 0 <= 1 -> false
        else -> super.onTouchEvent(event)
    }
}