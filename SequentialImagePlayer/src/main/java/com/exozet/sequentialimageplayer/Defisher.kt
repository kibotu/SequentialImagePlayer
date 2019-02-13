package com.exozet.sequentialimageplayer

import android.graphics.Bitmap
import android.util.Log


/**
 * http://www.tannerhelland.com/4743/simple-algorithm-correcting-lens-distortion/
 */
fun RemoveFishEye(bitmap: Bitmap, strength: Double): Bitmap {

    Log.v("Defishing", "start...")
    val start = System.currentTimeMillis()

    val conf = Bitmap.Config.ARGB_8888 // see other conf types
    val correctedImage = Bitmap.createBitmap(bitmap.width, bitmap.height, conf) // this creates a MUTABLE bitmap

    //The center points of the image
    val xc = bitmap.width / 2.0
    val yc = bitmap.height / 2.0

    var s = strength
    if (strength == 0.0)
        s = 0.00001

    val correctionRadius = Math.sqrt((correctedImage.width * correctedImage.width + correctedImage.height * correctedImage.height).toDouble()) / s

    var theta: Double
    val zoom = 1.0

    val src = DrawableBitmapContainer(bitmap)
    val dst = DrawableBitmapContainer(correctedImage)

    for (x in 0 until correctedImage.width) {
        for (y in 0 until correctedImage.height) {

            val newX = x - xc
            val newY = y - yc

            val distance = Math.sqrt(newX * newX + newY * newY)
            val r = distance / correctionRadius

            theta = if (r == 0.0)
                1.0
            else
                Math.atan(r) / r

            val sourceX = xc + theta * newX * zoom
            val sourceY = yc + theta * newY * zoom

            val xd = Math.max(0.0, Math.min(sourceX, (correctedImage.width - 1).toDouble())).toInt()
            val yd = Math.max(0.0, Math.min(sourceY, (correctedImage.height - 1).toDouble())).toInt()
            dst.setPixel(x, y, src.getPixel(xd, yd))
        }
    }

    Log.v("Defishing", "done after " + (System.currentTimeMillis() - start) + " ms")

    return dst.bimap
}

class DrawableBitmapContainer(private val image: Bitmap) {

    private val width: Int = image.width

    private val height: Int = image.height

    private val pixels: IntArray

    val bimap: Bitmap
        get() {
            image.setPixels(pixels, 0, width, 0, 0, width, height)
            return image
        }

    init {
        pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)
    }

    fun getPixel(x: Int, y: Int): Int {
        return pixels[x + y * width]
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        pixels[x + y * width] = color
    }
}