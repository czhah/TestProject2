package com.zzmeng.customcamera.ui.widget.mycamera

object CameraUtils {

    fun getSensorAngle(x: Float, y: Float): Int {
        return if (Math.abs(x) > Math.abs(y)) {
            /**
             * 横屏倾斜角度比较大
             */
            when {
                x > 7 ->
                    /**
                     * 左边倾斜
                     */
                    270
                x < -7 ->
                    /**
                     * 右边倾斜
                     */
                    90
                else ->
                    /**
                     * 倾斜角度不够大
                     */
                    0
            }
        } else {
            when {
                y > 7 ->
                    /**
                     * 左边倾斜
                     */
                    0
                y < -7 ->
                    /**
                     * 右边倾斜
                     */
                    180
                else ->
                    /**
                     * 倾斜角度不够大
                     */
                    0
            }
        }
    }
}