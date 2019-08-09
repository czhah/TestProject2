package com.dosmono.customcamera.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.*

internal object BitmapUtils {

    /**
     * byte转bitmap
     *
     * @param b
     * @return
     */
    fun getBitmapFromBytes(b: ByteArray, width: Int, height: Int): Bitmap? {
        if (b.isEmpty()) return null
        val degree = getBitmapDegree(b)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(b, 0, b.size, options)
        val realWidth = options.outWidth.toFloat()
        val realHeight = options.outHeight.toFloat()
        var scale = 1
        //  压缩对比
        if (width > 0 && height > 0) {
            scale = Math.max(realWidth / width, realHeight / height).toInt()
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = if (scale < 1) 1 else scale
        options.inPreferredConfig = Bitmap.Config.RGB_565
        var bitmap: Bitmap? = BitmapFactory.decodeByteArray(b, 0, b.size, options)
        if (degree != 0 && bitmap != null) {
            val degreeBitmap = adjustPhotoRotation(bitmap, degree)
            if (degreeBitmap != null) {
                bitmap.recycle()
                bitmap = null
                return degreeBitmap
            }
        }
        return bitmap
    }

    fun getBitmapDegree(b: ByteArray): Int {
        val bis = ByteArrayInputStream(b)
        var degree = 0
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(bis)
            // 获取图片的旋转信息
            when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    /**
     * 图片旋转
     * @param bm
     * @param orientationDegree
     * @return
     */
    fun adjustPhotoRotation(bm: Bitmap, orientationDegree: Int): Bitmap {
        var returnBm: Bitmap? = null
        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        if (orientationDegree == 270 && Build.BRAND == "Xiaomi") {
            matrix.setScale(1f, -1f)
        }
        matrix.postRotate(orientationDegree.toFloat())
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(
                bm, 0, 0, bm.width,
                bm.height, matrix, true
            )
        } catch (e: OutOfMemoryError) {
        }
        if (returnBm == null) {
            returnBm = bm
        }
        if (bm != returnBm) {
            bm.recycle()
        }
        return returnBm
    }

    /**
     * 保存图片
     *
     * @param bm
     * @param path
     * @return
     */
    fun bitmap2Path(bm: Bitmap, path: String): Boolean {
        var out: FileOutputStream? = null
        try {
            val f = File(path)
            out = FileOutputStream(f)
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }
}