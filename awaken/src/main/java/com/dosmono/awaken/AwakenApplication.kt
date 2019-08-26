package com.dosmono.awaken

import android.app.Application
import android.util.Log
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility

object AwakenApplication {

    fun init(application: Application) {


        SpeechUtility.createUtility(application, SpeechConstant.APPID + "=5d57ae5d")


        Log.d("cz", "${SpeechUtility.getUtility()}")
    }
}