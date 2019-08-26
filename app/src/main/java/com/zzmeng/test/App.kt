package com.zzmeng.test

import android.app.Application
import android.content.Context
import com.dosmono.awaken.AwakenApplication


/**
 *  @项目名：  TestProject
 *  @创建者:   chenzhuaung
 *  @创建时间:  2019/8/19 9:48
 *  @描述：    App
 */
class App: Application(){

    lateinit var context: Context
    lateinit var application: Application

    override fun onCreate() {
        super.onCreate()
        context = this
        application = this

        AwakenApplication.init(application)
    }
}