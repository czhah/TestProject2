package com.zzmeng.test.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zzmeng.test.R
import kotlinx.android.synthetic.main.activity_my_camera.*

class MyCameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_camera)

        btn_take.setOnClickListener {
            cameraView.takePicture()
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }
}
