package com.exozet.sequentialimageplayer.glrippleview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.GLUtils
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.exozet.sequentialimageplayer.R
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Ryota Niinomi on 2017/05/24.
 */
internal class RippleRenderer(private val context: Context,
                              private var bgImages: MutableList<Bitmap>) : GLSurfaceView.Renderer {

    companion object {
        private val NS_PER_SECOND = TimeUnit.SECONDS.toNanos(1).toFloat()
        private const val NO_TEXTURE = -1

        private val VERTICES: FloatArray = floatArrayOf(
                -1.0f, 1.0f, 0.0f, 1.0f,  // ↖ left top︎
                -1.0f, -1.0f, 0.0f, 1.0f, // ↙︎ left bottom
                1.0f, 1.0f, 0.0f, 1.0f,   // ↗︎ right top
                1.0f, -1.0f, 0.0f, 1.0f   // ↘︎ right bottom
        )
    }

    var debug = false

    private val renderInfoList: MutableList<RenderInfo> = mutableListOf()
    private var width: Float = 0f
    private var height: Float = 0f
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var fadeAnimator: ValueAnimator? = null
    private var currentImageIndex: Int = 0

    var rippleOffset: Float = 0f
    var strength: Float = 0.00001f
    var zoom: Float = 1f
    var point: Pair<Float, Float> = Pair(0f, 0f)
    var fadeDuration: Long = 2000
    var fadeInterval: Long = 5000

    val actions: ConcurrentLinkedQueue<Runnable> = ConcurrentLinkedQueue()

    init {
        setRenderInfoList()
    }

    @Volatile
    var currentTextureId: Int = NO_TEXTURE

    fun setBackground(image: Bitmap) {
        actions.add(Runnable {
            loadTexture(image)
            Log.v(this::class.java.simpleName, "setBackground=$currentTextureId")
        })
//        renderInfoList[0].textureId = loadTexture(image, renderInfoList[0].textureId)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        doAllRenderInfo { renderInfo ->
            try {
                renderInfo.programId = GLES20.glCreateProgram()
                logGlError(gl, "glCreateProgram ${renderInfo.programId}")

                GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).let { vertexShader ->
                    GLES20.glShaderSource(vertexShader, loadRawResource(context, R.raw.ripple_vertex))
                    GLES20.glCompileShader(vertexShader)
                    GLES20.glAttachShader(renderInfo.programId, vertexShader)
                    logGlError(gl, "glAttachShader vertexShader ${renderInfo.programId}")
                }

                GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).let { fragmentShader ->
                    GLES20.glShaderSource(fragmentShader, loadRawResource(context, R.raw.defish_fragment))
                    GLES20.glCompileShader(fragmentShader)
                    GLES20.glAttachShader(renderInfo.programId, fragmentShader)
                    logGlError(gl, "glAttachShader fragmentShader ${renderInfo.programId}")
                }

                GLES20.glLinkProgram(renderInfo.programId)
                logGlError(gl, "glLinkProgram ${renderInfo.programId}")
                GLES20.glUseProgram(renderInfo.programId)
                logGlError(gl, "glUseProgram ${renderInfo.programId}")
            } catch (e: IOException) {
                Log.e(this.javaClass.name, e.message.orEmpty())
            }

            loadTexture(renderInfo.bgImage)
            logGlError(gl, "loadTexture ${currentTextureId}")
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = height.toFloat()
        this.height = width.toFloat()

        GLES20.glViewport(0, 0, width, height)

        setTexCoordBuffer()
    }

    override fun onDrawFrame(gl: GL10?) {

        while (actions.isNotEmpty())
            actions.poll()?.run()

        if (currentTextureId == NO_TEXTURE)
            return

        logGlError(gl, "onDrawFrame")

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        logGlError(gl, "glClear")

        doCurrentAndNextRenderInfo { renderInfo ->

            logGlError(gl, "doCurrentAndNextRenderInfo")

            // position
            val position: Int = GLES20.glGetAttribLocation(renderInfo.programId, "position")
            GLES20.glEnableVertexAttribArray(position)
            GLES20.glVertexAttribPointer(position, 4, GLES20.GL_FLOAT, false, 0, renderInfo.vertexBuffer)
            logGlError(gl, "Set Position")

            logGlError(gl, "glEnable GL_TEXTURE_2D")

            // texCoord
            val texCoord: Int = GLES20.glGetAttribLocation(renderInfo.programId, "texcoord")
            GLES20.glEnableVertexAttribArray(texCoord)
            GLES20.glVertexAttribPointer(texCoord, 2, GLES20.GL_FLOAT, false, 0, renderInfo.texcoordBuffer)
            logGlError(gl, "Set texCoord")

            // texture
            GLES20.glGetUniformLocation(renderInfo.programId, "texture").run {
                GLES20.glUniform1i(this, 0)
            }
            logGlError(gl, "Set texture")

            // resolution
            GLES20.glGetUniformLocation(renderInfo.programId, "textureSize").run {
                GLES20.glUniform2f(this, bgImages.first().width.toFloat(), bgImages.first().height.toFloat())
            }
            logGlError(gl, "Set textureSize")

            // time
            GLES20.glGetUniformLocation(renderInfo.programId, "time").run {
                val now = System.nanoTime()
                val delta = now / NS_PER_SECOND
                GLES20.glUniform1f(this, delta)
            }
            logGlError(gl, "Set time")

            GLES20.glGetUniformLocation(renderInfo.programId, "alpha").run {
                GLES20.glUniform1f(this, renderInfo.alpha)
            }
            logGlError(gl, "Set alpha")

            GLES20.glGetUniformLocation(renderInfo.programId, "strength").run {
                GLES20.glUniform1f(this, strength)
            }
            logGlError(gl, "Set strength")

            GLES20.glGetUniformLocation(renderInfo.programId, "zoom").run {
                GLES20.glUniform1f(this, zoom)
            }
            logGlError(gl, "Set zoom")

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            logGlError(gl, "glEnable GL_TEXTURE0")
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, currentTextureId)
            logGlError(gl, "Set textureId ${currentTextureId}")
//            GLES20.glEnable(GLES20.GL_TEXTURE_2D)
            logGlError(gl, "glEnable GL_TEXTURE_2D")
            GLES20.glEnable(GLES20.GL_BLEND)
            logGlError(gl, "glEnable GL_BLEND")
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            logGlError(gl, "glBlendFunc")
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            logGlError(gl, "glDrawArrays")

            // disable
            GLES20.glDisableVertexAttribArray(position)
            logGlError(gl, "glDisableVertexAttribArray position")
            GLES20.glDisableVertexAttribArray(texCoord)
            logGlError(gl, "glDisableVertexAttribArray texCoord")
            GLES20.glDisable(GLES20.GL_BLEND)
            logGlError(gl, "glDisable GL_BLEND")
//            GLES20.glDisable(GLES20.GL_TEXTURE_2D)
            logGlError(gl, "glDisable GL_TEXTURE_2D")
        }
    }

    /**
     * Add images for cross fade animation.
     */
    fun addBackgroundImages(images: List<Bitmap>) {
        bgImages.clear()
        bgImages.addAll(images)
        setRenderInfoList()
    }

    /**
     * Start cross fade animation after fade interval.
     */
    fun startCrossFadeAnimation() {
        if (renderInfoList.size < 2) {
            Log.i(javaClass.name, "Can not start cross-fade animation since renderInfoList size is under 2.")
            return
        }

        fadeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = fadeDuration
            addUpdateListener { animator ->
                val velocity: Float = animator.animatedValue as Float

                renderInfoList[currentImageIndex].alpha = 1f - velocity
                renderInfoList[getNextImageIndex()].alpha = velocity
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    currentImageIndex++

                    if (currentImageIndex > renderInfoList.size - 1) {
                        currentImageIndex = 0
                    }
                    startCrossFadeAnimation()
                }
            })
        }
        handler.postDelayed({
            fadeAnimator?.start()
        }, fadeInterval)
    }

    private fun loadTexture(img: Bitmap?, usedTexId: Int = currentTextureId, recycle: Boolean = false): Int {
        if (img == null)
            return NO_TEXTURE

        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE) {
            GLES20.glEnable(GLES20.GL_TEXTURE_2D)
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle && !img.isRecycled) {
//            img.recycle()
        }

        currentTextureId = textures[0]

        return textures[0]
    }

    @Throws(IOException::class)
    private fun loadRawResource(context: Context, id: Int): String {
        val inputStream: InputStream = context.resources.openRawResource(id)
        val l = inputStream.available()
        val b = ByteArray(l)
        return if (inputStream.read(b) == l) String(b) else ""
    }

    /**
     * This should be called from onSurfaceCreated().
     */
    private fun doAllRenderInfo(action: (RenderInfo) -> Unit) {
        for (i in (renderInfoList.size - 1) downTo 0) {
            action(renderInfoList[i])
        }
    }

    /**
     * This should be called from onDrawFrame().
     * For cross-fading, drawing current and next item is enough.
     */
    private fun doCurrentAndNextRenderInfo(action: (RenderInfo) -> Unit) {
        if (renderInfoList.size == 0) {
            return
        }
        if (renderInfoList.size >= 2) {
            action(renderInfoList[getNextImageIndex()])
        }
        action(renderInfoList[currentImageIndex])
    }

    private fun setRenderInfoList() {
        renderInfoList.clear()

        bgImages.forEachIndexed { index, bgImage ->
            renderInfoList.add(
                RenderInfo(
                    BufferUtil.convert(VERTICES),
                    null,
                    0,
                    NO_TEXTURE,
                    bgImage,
                    if (index == 0) 1f else 0f
            )
            )
        }
    }

    /**
     * Set texcoordBuffer to renderInfoList based on image size.
     */
    private fun setTexCoordBuffer() {
        renderInfoList.forEach { info ->

            var newWidth: Float = width
            var newHeight: Float = info.bgImage.height * newWidth / info.bgImage.width

            if (newHeight < height) {
                newHeight = height
                newWidth = info.bgImage.width * newHeight / info.bgImage.height
            }

            val rX: Float = (1f - width / newWidth) / 2f
            val rY: Float = (1f - height / newHeight) / 2f

            // Scale image with keep ratio.
            val texCoords: FloatArray = floatArrayOf(
                    0.0f + rX, 0.0f + rY, // ↖ left top︎
                    0.0f + rX, 1.0f - rY, // ↙︎ left bottom
                    1.0f - rX, 0.0f + rY, // ↗︎ right top
                    1.0f - rX, 1.0f - rY  // ↘︎ right bottom
            )
            info.texcoordBuffer = BufferUtil.convert(texCoords)
        }
    }

    /**
     * Get next index for background image.
     * If there is no next index, return 0.
     */
    private fun getNextImageIndex(): Int {
        return if (currentImageIndex + 1 > renderInfoList.size - 1) 0 else currentImageIndex + 1
    }

    private fun logGlError(gl: GL10?, text: String) {
        if (!debug)
            return
        if (gl == null)
            return
        val glError = gl.glGetError()
        if (glError != GL10.GL_NO_ERROR) {
            Log.e(this::class.java.simpleName, "GL ERROR: $text - ${GLU.gluErrorString(glError)} ${GLUtils.getEGLErrorString(glError)}")
        }
    }
}