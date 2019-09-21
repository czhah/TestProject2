package com.zzmeng.test.ui.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.zzmeng.test.R
import kotlinx.android.synthetic.main.activity_my_calendar.*
import java.util.*

/**
 * 日历控件
 */
class MyCalendarActivity : AppCompatActivity() {

    private var tempTime = System.currentTimeMillis()
    private val calendar = java.util.Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_calendar)

        calendarView.setOnYearChangeListener { year -> Log.d("cz", "===setOnYearChangeListener  year:$year") }
        calendarView.setOnMonthChangeListener{ year, month -> Log.d("cz", "===setOnMonthChangeListener year:$year, month:$month") }
        calendarView.setOnCalendarSelectListener(object: CalendarView.OnCalendarSelectListener{
            override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
                Log.d("cz", "===setOnCalendarSelectListener  onCalendarSelect:${Date(calendar.timeInMillis)} isClick:$isClick")
            }

            override fun onCalendarOutOfRange(calendar: Calendar?) {
            }

        })
        calendar.timeInMillis = tempTime
        calendarView.setOnCalendarInterceptListener(object: CalendarView.OnCalendarInterceptListener{
            override fun onCalendarIntercept(calendar: Calendar): Boolean {
                return calendar.timeInMillis > System.currentTimeMillis()
            }

            override fun onCalendarInterceptClick(calendar: Calendar, isClick: Boolean) {
                Toast.makeText(
                    this@MyCalendarActivity,
                    calendar.toString() + if (isClick) "拦截不可点击" else "拦截滚动到无效日期",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        btn_jump.setOnClickListener {
            calendar.timeInMillis = tempTime
            calendar.add(java.util.Calendar.MONTH, -1)
            tempTime = calendar.timeInMillis
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            Log.d("cz", "year:$year, month:$month, day:$day")
            calendarView.scrollToCalendar(year, month, day)
        }

        initData()
    }

    fun initData() {


        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        Log.d("cz", "===：${Date(calendar.timeInMillis)}")

        Log.d("cz", "====11111111")
        calendarView.setRange(calendar.get(java.util.Calendar.YEAR) - 50, 1, 1, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1, calendar.get(java.util.Calendar.DAY_OF_YEAR))
        calendarView.scrollToCurrent()
        Log.d("cz", "====2222222")


        val map = HashMap<String, Calendar>()
        map[getSchemeCalendar(calendarView.curYear, calendarView.curMonth, 4, Color.BLUE, "假").toString()] =
            getSchemeCalendar(calendarView.curYear, calendarView.curMonth, 4, Color.BLUE, "假")
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.addSchemeDate(map)
        Log.d("cz", "====33333333")

        calendarView.removeMultiSelect(getSchemeCalendar(2019, 9, 6, Color.BLUE, "假"))
        calendarView.postDelayed(Runnable {
            calendarView.clearMultiSelect()
        }, 200)
    }

    private fun getSchemeCalendar(year: Int, month: Int, day: Int, color: Int, text: String): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color//如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        return calendar
    }
}
