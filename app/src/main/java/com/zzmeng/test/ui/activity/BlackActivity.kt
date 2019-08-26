package com.zzmeng.test.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.zzmeng.test.R
import com.zzmeng.test.config.Constant
import kotlinx.android.synthetic.main.activity_black.*

class BlackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black)

        btn_show.setOnClickListener { LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constant.ACTION_SHOW)) }
    }
}
