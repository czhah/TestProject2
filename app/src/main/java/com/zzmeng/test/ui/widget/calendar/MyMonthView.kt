package com.zzmeng.test.ui.widget.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_my_calendar.*
import kotlinx.android.synthetic.main.activity_my_calendar.view.*


/**
 *  @项目名：  meeting
 *  @创建者:   chenzhuaung
 *  @创建时间:  2019/8/26 19:26
 *  @描述：    MyMonthView
 */
class MyMonthView: MonthView {

    private var mRadius: Int = 0

    private val mDisablePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mDisablePaint.color = Color.GRAY
        mDisablePaint.isAntiAlias = true
        mDisablePaint.style = Paint.Style.FILL
        mDisablePaint.textAlign = Paint.Align.CENTER
        mDisablePaint.isFakeBoldText = true
    }

    constructor(context: Context): super(context) {

        //兼容硬件加速无效的代码
        setLayerType(View.LAYER_TYPE_SOFTWARE, mSelectedPaint)
        //4.0以上硬件加速会导致无效
        mSelectedPaint.maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.SOLID)

    }

    override fun onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.STROKE
    }

    override fun onLoopStart(x: Int, y: Int) {

    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        Log.d("cz", "====onDrawSelected")
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSchemePaint)
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2

        when {
            onCalendarIntercept(calendar) -> {
                mDisablePaint.textSize = mSelectTextPaint.textSize
                canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, mDisablePaint)
            }

            isSelected -> {
                Log.d("cz", "====isSelected")
                canvas.drawText(
                    calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    mSelectTextPaint
                )
            }
//            hasScheme -> canvas.drawText(
//                calendar.day.toString(),
//                cx.toFloat(),
//                baselineY,
//                when {
//                    calendar.isCurrentDay -> mCurDayTextPaint
//                    calendar.isCurrentMonth -> mSchemeTextPaint
//                    else -> mOtherMonthTextPaint
//                }
//            )
            else -> canvas.drawText(
                calendar.day.toString(), cx.toFloat(), baselineY,
                when {
                    calendar.isCurrentDay -> mCurDayTextPaint
                    calendar.isCurrentMonth -> mCurMonthTextPaint
                    else -> mOtherMonthTextPaint
                }
            )
        }

        //日期是否可用？拦截
//        if (onCalendarIntercept(calendar)) {
//            mDisablePaint.textSize = mCurDayTextPaint.textSize
//            canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, mDisablePaint)
//        }
    }
}