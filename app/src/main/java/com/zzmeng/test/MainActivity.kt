package com.zzmeng.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Window
import com.zzmeng.customcamera.ui.activity.CustomCameraActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_content.setOnClickListener {
            val intent = Intent(this, CustomCameraActivity::class.java)
            val file = File(Environment.getExternalStorageDirectory(), "a.jpg")
            if (!file.exists()) file.createNewFile()
            intent.putExtra(CustomCameraActivity.PARAMS1, file.absolutePath)
            intent.putExtra(CustomCameraActivity.PARAMS2, false)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }
}
