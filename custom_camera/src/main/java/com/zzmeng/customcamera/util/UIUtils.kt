package com.dosmono.customcamera.util

import android.app.Activity
import android.content.Context
import android.graphics.Point

internal object UIUtils {

    /**
     * 获取屏幕宽度
     *
     * @param mContext
     * @return
     */
    fun getScreenWidth(mContext: Context): Int {
        val display = (mContext as Activity).windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.x
    }

    /**
     * 获取屏幕高度
     *
     * @param mContext
     * @return
     */
    fun getScreenHeight(mContext: Context): Int {
        val display = (mContext as Activity).windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.y
    }

    /**
     * 把dp转成px
     *
     * @param context
     * @param dpVal
     * @return
     */
    fun dp2px(context: Context, dpVal: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpVal * scale + 0.5f).toInt()
    }

    /**
     * 获取最大公约数
     *
     * @param a
     * @param b
     * @return
     */
    fun mGCD(a: Int, b: Int): Int {
        return if (a % b == 0) b else mGCD(b, a % b)
    }

}