package com.exozet.sequentialimageplayer

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.sequentialimageplayer_view.view.*
import java.io.IOException
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class SequentialImagePlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @LayoutRes
    val layout = R.layout.sequentialimageplayer_view

    private val TAG by lazy { "${this::class.java.simpleName}:$uuid" }

    private val uuid: String by lazy { UUID.randomUUID().toString().take(8) }

    var debug = false

    private fun log(message: String) {
        if (debug)
            Log.d(TAG, message)
    }

    private var imageSwapper: ImageSwapper? = null

    var imageUris: Array<Uri> = arrayOf()
        set(value) {
            if (field contentEquals value)
                return
            field = value

            loadImage(value.firstOrNull())
        }

    @FloatRange(from = -1.0, to = 1.0)
    var swipeSpeed: Float = 0.75f

    var autoPlay: Boolean = false
        get() = autoplaySwitch.isChecked
        set(value) {
            field = value
            autoplaySwitch.isChecked = value
        }

    internal val max
        get() = imageUris.size - 1

    var showControls: Boolean = false
        set(value) {
            field = value
            seekBar.goneUnless(!value)
            playDirectionSwitch.goneUnless(!value)
            autoplaySwitch.goneUnless(!value)
            fpsSpinner.goneUnless(!value)
        }

    var progress: Float = 0f
        get () {
            return (imageSwapper?.index?.toFloat() ?: 1f) / max
        }
        set(value) {
            field = value
            onProgressChanged?.invoke(value)
        }

    var onProgressChanged: ((Float) -> Unit)? = null

    @IntRange(from = 1, to = 60)
    var fps: Int = 30
        set(value) {
            field = value
            with(fpsSpinner) {
                adapter =
                    ArrayAdapter(context, android.R.layout.simple_spinner_item, (1 until 61).map { "$it" }.toList())
                setSelection(value - 1)
            }
        }

    var zoomable: Boolean = true
        set(value) {
            field = value
            viewHolder.isZoomable = value
        }

    var translatable: Boolean = true
        set(value) {
            field = value
            viewHolder.isTranslatable = value
        }

    var playBackwards: Boolean = true
        set(value) {
            field = value
            playDirectionSwitch.isChecked = value
        }

    var blurLetterbox: Boolean = true

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(layout, this, true)

        onCreate()
    }

    private fun onCreate() {
        imageSwapper = ImageSwapper(this)

        initSeekBar()

        autoplaySwitch.setOnCheckedChangeListener { _, isChecked -> if (isChecked) startAutoPlay() else stopAutoPlay() }

        initFpsSpinner()

        addSwipeGesture()

        cancelBusy()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) onResume() else onPause()
    }

    private fun initFpsSpinner() {
        fpsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                fps = p2
            }
        }
    }

    /**
     * Converts dp to pixel.
     */
    private val Float.px: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            context!!.resources.displayMetrics
        )

    private fun addSwipeGesture() {

        var startScrollingSeekPosition = 0

        swipe_detector.scrollListener.thresholdX = 3f.px
        swipe_detector.scrollListener.thresholdY = 3f.px

        // connect swipe detector
        viewHolder.view = swipe_detector

        // toggle video playback based on scrolling state
        swipe_detector?.onIsScrollingChanged {
            log("onIsScrollingChanged isScrolling=$it")
            if (it) {
                if (autoPlay) stopAutoPlay()
                startScrollingSeekPosition = imageSwapper?.index ?: 0
            } else {
                if (autoPlay) startAutoPlay()
            }

            log("onIsScrollingChanged viewHolder.isZoomable=${viewHolder.isZoomable} viewHolder.isTranslatable=${viewHolder.isTranslatable} zoomable=$zoomable translatable=$translatable")
        }

        swipe_detector?.onScroll { percentX, percentY ->

            val duration = max
            val currentPosition = imageSwapper?.index ?: 0

            val maxPercent = swipeSpeed
            val scaledPercent = percentX * maxPercent
            val percentOfDuration = scaledPercent * -1 * duration + startScrollingSeekPosition
            // shift in position domain and ensure circularity
            val newSeekPosition = ((percentOfDuration + duration) % duration).roundToInt().absoluteValue
            log("onScroll percentX=$percentX scaledPercent=$scaledPercent currentPosition=$currentPosition newPosition=newSeekPosition duration=$duration")

            imageSwapper?.swapImage(newSeekPosition)
        }
    }

    private fun initSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageSwapper?.swapImage(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (autoPlay) stopAutoPlay()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (autoPlay) startAutoPlay()
            }
        })
    }

    fun onResume() {
        if (autoPlay) startAutoPlay()
    }


    fun onPause() {
        if (autoPlay) stopAutoPlay()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewHolder.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private fun startAutoPlay() {
        viewHolder.removeCallbacks(null)
        viewHolder.post(imageSwapper)
    }

    private fun stopAutoPlay() {
        viewHolder.removeCallbacks(imageSwapper)
    }

    internal fun loadImage(uri: Uri?) {
        if (uri == null)
            return

        when {
            uri.toString().startsWith("file:///android_asset/") -> {
                with(loadBitmap(uri)) {
                    viewHolder.setImageBitmap(this)
                    blurWith(this)
                }
            }
            uri.toString().startsWith("http://") -> {
                setImageWithGlide(uri)
            }
            uri.toString().startsWith("https://") -> {
                setImageWithGlide(uri)
            }
            else -> {
                with(loadBitmap(uri)) {
                    viewHolder.setImageBitmap(this)
                    blurWith(this)
                }
            }
        }
    }

    val requestOptions by lazy {
        RequestOptions
            .fitCenterTransform()
            .priority(Priority.HIGH)
            .dontAnimate()
            .skipMemoryCache(false)
            .override(viewHolder.width, viewHolder.height)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    private fun setImageWithGlide(uri: Uri?) {
        Glide.with(this)
            .asBitmap()
            .load(uri)
            .apply(requestOptions)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    viewHolder.setImageBitmap(resource)
                    blurWith(resource)
                }
            })
    }

    private fun blurWith(bitmap: Bitmap?) {
        if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            if (bitmap?.width ?: 0 >= bitmap?.height ?: 0)
                viewHolderBackground.blur(bitmap)
        }

        if (resources.configuration.orientation == ORIENTATION_LANDSCAPE)
            if (bitmap?.height ?: 0 >= bitmap?.width ?: 0)
                viewHolderBackground.blur(bitmap)
    }

    private fun loadBitmap(uri: Uri?): Bitmap? {

        var bitmap: Bitmap? = null
        try {

            val istr = if (uri.toString().startsWith("file:///android_asset/"))
                context.assets.open(uri.toString().removePrefix("file:///android_asset/"))
            else
                context.contentResolver.openInputStream(uri)

            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    internal fun cancelBusy() {
        progressBar.visibility = View.GONE
    }

    internal fun busy() {
        progressBar.visibility = View.VISIBLE
    }

    companion object {

        internal const val FPS = "FPS"
        internal const val ZOOMABLE = "ZOOMABLE"
        internal const val TRANSLATABLE = "TRANSLATABLE"
        internal const val PLAY_BACKWARDS = "PLAY_BACKWARDS"
        internal const val AUTO_PLAY = "AUTO_PLAY"
        internal const val SHOW_CONTROLS = "SHOW_CONTROLS"
        internal const val SWIPE_SPEED = "SWIPE_SPEED"
        internal const val BLUR_LETTERBOX = "BLUR_LETTERBOX"

        internal fun loopRange(value: Int, min: Int = 0, max: Int): Int = when {
            value > max -> min + Math.abs(value)
            value < min -> max - Math.abs(value)
            else -> value
        }

        internal fun View.goneUnless(isGone: Boolean = true) {
            visibility = if (isGone) View.GONE else View.VISIBLE
        }
    }

    private val paint = Paint().apply { flags = Paint.FILTER_BITMAP_FLAG }

    private var blurryBitmap: Bitmap? = null


    private fun ImageView.blur(bitmap: Bitmap?, radius: Int = 10, scaleFactor: Float = 8f) {
        if (!blurLetterbox)
            return

        if (bitmap == null)
            return

        var width = measuredWidth
        var height = measuredHeight

        if (width <= 0 || height <= 0) {
            width = context?.resources?.configuration?.screenWidthDp?.toFloat()?.px?.toInt() ?: 0
            height = context?.resources?.configuration?.screenHeightDp?.toFloat()?.px?.toInt() ?: 0
        }

        if (width <= 0 || height <= 0) {
            return
        }

        val startMs = System.currentTimeMillis()

        if (blurryBitmap == null)
            blurryBitmap = Bitmap.createBitmap(
                (width / scaleFactor).toInt(),
                (height / scaleFactor).toInt(),
                Bitmap.Config.RGB_565
            )

        val canvas = Canvas(blurryBitmap)
        canvas.translate(-left.toFloat() + -measuredWidth / 2f, -top.toFloat() / 2f)
//        canvas.scale(1 / scaleFactor, 1 / scaleFactor)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        blurryBitmap = FastBlur.doBlur(blurryBitmap, radius, true)

        setImageBitmap(blurryBitmap)

        // log("view=[$measuredWidth:$measuredHeight]: bitmap=[${bitmap.width}:${bitmap.height}] overlay=[${blurryBitmap?.width}:${blurryBitmap?.height}] in ${System.currentTimeMillis() - startMs} ms")
    }
}