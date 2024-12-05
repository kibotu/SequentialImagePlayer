package com.exozet.sequentialimageplayer

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.transition.Fade
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.exozet.sequentialimageplayer.databinding.SequentialimageplayerViewBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class SequentialImagePlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: SequentialimageplayerViewBinding

    private val TAG by lazy { "${this::class.java.simpleName}:$uuid" }

    private val uuid: String by lazy { UUID.randomUUID().toString().take(8) }

    var debug = false

    private fun log(message: String) {
        if (debug)
            Log.d(TAG, message)
    }

    var imageUris: Array<Uri> = arrayOf()
        set(value) {
            if (field contentEquals value)
                return
            field = value
            preload()
        }

    val requestOptions by lazy {

        RequestOptions
            .fitCenterTransform()
            .priority(Priority.IMMEDIATE)
            .dontAnimate()
            .override(1024)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    private var preloadCounter = 0

    private fun preload() {
        preloadCounter = 0

        binding.numberProgressBar.max = imageUris.size

        busy()

        imageUris.forEach {

            Glide.with(context!!.applicationContext!!)
                .applyDefaultRequestOptions(requestOptions)
                .load(it)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        onProgressDownload()
                        log("preload onLoadFailed count=$preloadCounter size=${imageUris.size} $model")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        onProgressDownload()
                        log("preload onResourceReady count=$preloadCounter size=${imageUris.size} $model")
                        return false
                    }
                })
                .preload()
        }
    }

    private fun onProgressDownload() {
        ++preloadCounter
        binding.numberProgressBar.progress = preloadCounter
        if (preloadCounter == imageUris.size) {
            cancelBusy()
            loadImage(imageUris.first())
            fadeInSwipeView()
        }
        binding.numberProgressBar.invalidate()
        invalidate()
    }

    @FloatRange(from = -1.0, to = 1.0)
    var swipeSpeed: Float = 0.75f

    var autoPlay: Boolean = false
        get() = binding.autoplaySwitch.isChecked
        set(value) {
            field = value
            binding.autoplaySwitch.isChecked = value
        }

    internal val max
        get() = imageUris.size - 1

    var showControls: Boolean = false
        set(value) {
            field = value
            binding.seekBar.goneUnless(!value)
            binding.playDirectionSwitch.goneUnless(!value)
            binding.autoplaySwitch.goneUnless(!value)
            binding.fpsSpinner.goneUnless(!value)
        }

    var progress: Float = 0f
        get() = currentItem / max.toFloat()
        set(value) {
            field = value
            onProgressChanged?.invoke(value)
        }

    var onProgressChanged: ((Float) -> Unit)? = null

    @IntRange(from = 1, to = 60)
    var fps: Int = 30
        set(value) {
            field = value
            log("fps=$field")
        }

    var duration: Int = 10000
        set(value) {
            field = value
            fps = (max.toFloat() / value * 1000f).roundToInt()
            log("duration=$field max=$max fps=$fps")
        }

    var zoomable: Boolean = true
        set(value) {
            field = value
            binding.viewHolder.isZoomable = value
        }

    var translatable: Boolean = true
        set(value) {
            field = value
            binding.viewHolder.isTranslatable = value
        }

    var playBackwards: Boolean = true
        set(value) {
            field = value
            binding.playDirectionSwitch.isChecked = value
        }

    var blurLetterbox: Boolean = true

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = SequentialimageplayerViewBinding.inflate(inflater, this, true)

        onCreate()
    }

    private fun onCreate() {

        binding.initSeekBar()

        binding.autoplaySwitch.setOnCheckedChangeListener { _, isChecked -> if (isChecked) startAutoPlay() else stopAutoPlay() }

        binding.addSwipeGesture()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) onResume() else onPause()
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

    private fun SequentialimageplayerViewBinding.addSwipeGesture() {

        var startScrollingSeekPosition = 0

        swipeDetector.scrollListener.thresholdX = 3f.px
        swipeDetector.scrollListener.thresholdY = 3f.px

        // connect swipe detector
        viewHolder.view = swipeDetector

        // toggle video playback based on scrolling state
        swipeDetector?.onIsScrollingChanged {
            log("onIsScrollingChanged isScrolling=$it")
            if (it) {
                if (swapIconView.visibility == View.VISIBLE) fadeOutSwipeView()
                if (autoPlay) stopAutoPlay()
                startScrollingSeekPosition = currentItem
            } else {
                if (autoPlay) startAutoPlay()
            }

            log("onIsScrollingChanged autoPlay=$autoPlay viewHolder.isZoomable=${viewHolder.isZoomable} viewHolder.isTranslatable=${viewHolder.isTranslatable} zoomable=$zoomable translatable=$translatable")
        }

        swipeDetector?.onScroll { percentX, percentY ->
            val maxPercent = swipeSpeed
            val scaledPercent = percentX * maxPercent
            val percentOfDuration = scaledPercent * -1 * max + startScrollingSeekPosition
            // shift in position domain and ensure circularity
            val newSeekPosition = ((percentOfDuration + max) % max).roundToInt().absoluteValue
            swapImage(newSeekPosition)
        }
    }

    private fun SequentialimageplayerViewBinding.initSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                swapImage(progress)
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
        binding.viewHolder.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private var subscription = CompositeDisposable()

    var currentItem = 0

    fun swapImage(index: Int) {
        if (max <= 0)
            return

        currentItem = (index + max) % max
        loadImage(imageUris[currentItem])
        binding.seekBar.progress = currentItem
        binding.seekBar.max = max

        onProgressChanged?.invoke(progress)
    }

    fun nextImage() {
        currentItem =
            if (!binding.playDirectionSwitch.isChecked) currentItem + 1 else currentItem - 1
        swapImage(currentItem)
    }

    val interval by lazy {
        val intervalDuration = (1000f / fps).roundToLong()
        log("intervalDuration=$intervalDuration")
        Observable.interval(intervalDuration, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { log("${it.message}") }
    }

    val intervalSubject by lazy {
        PublishSubject.create<Long>().also {
            interval.subscribe(it)
        }
    }

    var disposable: Disposable? = null

    fun startAutoPlay() {
        stopAutoPlay()
        log("startAutoPlay")

        disposable = intervalSubject.subscribe {
            nextImage()
        }.also {
            subscription.add(it)
        }
    }

    fun stopAutoPlay() {
        log("stopAutoPlay")
        if (disposable?.isDisposed == false) {
            log("stopAutoPlay dispose")
            disposable?.dispose()
        }
    }

    fun loadImage(uri: Uri?) {
        if (uri == null)
            return

        Glide.with(this)
            .asBitmap()
            .load(uri)
            .apply(requestOptions)
            .transition(withCrossFade(crossFadeFactory))
            .into(bitmapImageViewTarget)
    }

    private val crossFadeFactory by lazy {
        DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    }

    private val bitmapImageViewTarget = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            binding.viewHolder.setImageBitmap(resource)
            blurWith(resource)
        }
    }

    private fun blurWith(bitmap: Bitmap?) {
        if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            if ((bitmap?.width ?: 0) >= (bitmap?.height ?: 0))
                binding.viewHolderBackground.blur(bitmap)
        }

        if (resources.configuration.orientation == ORIENTATION_LANDSCAPE)
            if ((bitmap?.height ?: 0) >= (bitmap?.width ?: 0))
                binding.viewHolderBackground.blur(bitmap)
    }

    private fun loadBitmap(uri: Uri?): Bitmap? {

        var bitmap: Bitmap? = null
        try {

            val istr = if (uri.toString().startsWith("file:///android_asset/"))
                context.assets.open(uri.toString().removePrefix("file:///android_asset/"))
            else
                context.contentResolver.openInputStream(uri!!)

            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    internal fun cancelBusy() {
        binding.numberProgressBar.visibility = View.GONE
    }

    internal fun busy() {
        binding.numberProgressBar.visibility = View.VISIBLE
    }

    companion object {

        internal const val FPS = "FPS"
        internal const val DURATION = "DURATION"
        internal const val ZOOMABLE = "ZOOMABLE"
        internal const val TRANSLATABLE = "TRANSLATABLE"
        internal const val PLAY_BACKWARDS = "PLAY_BACKWARDS"
        internal const val AUTO_PLAY = "AUTO_PLAY"
        internal const val SHOW_CONTROLS = "SHOW_CONTROLS"
        internal const val SWIPE_SPEED = "SWIPE_SPEED"
        internal const val BLUR_LETTERBOX = "BLUR_LETTERBOX"

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

        if (blurryBitmap == null) {
            blurryBitmap = Bitmap.createBitmap(
                (width / scaleFactor).toInt(),
                (height / scaleFactor).toInt(),
                Bitmap.Config.RGB_565
            )
        }

        val bitmap = blurryBitmap ?: return

        val canvas = Canvas(bitmap)
        canvas.translate(-left.toFloat() + -measuredWidth / 2f, -top.toFloat() / 2f)
//        canvas.scale(1 / scaleFactor, 1 / scaleFactor)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        blurryBitmap = FastBlur.doBlur(bitmap, radius, true)

        setImageBitmap(blurryBitmap)

        // log("view=[$measuredWidth:$measuredHeight]: bitmap=[${bitmap.width}:${bitmap.height}] overlay=[${blurryBitmap?.width}:${blurryBitmap?.height}] in ${System.currentTimeMillis() - startMs} ms")
    }

    private fun fadeInSwipeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(binding.swapIconView, Fade(Fade.OUT))
        }
        binding.swapIconView.visibility = View.VISIBLE
    }

    private fun fadeOutSwipeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(binding.swapIconView, Fade(Fade.OUT))
        }
        binding.swapIconView.visibility = View.GONE
    }
}