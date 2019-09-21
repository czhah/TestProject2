package com.zzmeng.test.ui.activity

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dosmono.awaken.ui.activity.OneShotDemoActivity
import com.dosmono.awaken.ui.activity.WakeDemoActivity
import com.zzmeng.test.R
import com.zzmeng.test.config.Constant
import com.zzmeng.test.ui.dialog.DialogFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var aiDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_window.setOnClickListener { startActivity(Intent(this, BlackActivity::class.java)) }

        btn_dialog.setOnClickListener { showAI() }

        btn_wake.setOnClickListener { startActivity(Intent(this, WakeDemoActivity::class.java)) }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(Constant.ACTION_SHOW))

        btn_calendar.setOnClickListener { startActivity(Intent(this, MyCalendarActivity::class.java)) }

        btn_volume.setOnClickListener { startActivity(Intent(this, MyVolumeActivity::class.java)) }

        btn_camera.setOnClickListener { startActivity(Intent(this, MyCameraActivity::class.java)) }
    }


    private val receiver:BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                Constant.ACTION_SHOW -> {
                    showAI()
                }
                else -> {}
            }
        }
    }

    private fun showAI() {
        if(aiDialog != null && aiDialog!!.isShowing) {
            aiDialog!!.dismiss()
        }
        aiDialog = DialogFactory.createAiDialog(this@MainActivity)
        aiDialog!!.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}
