package com.dosmono.awaken.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dosmono.awaken.R
import kotlinx.android.synthetic.main.activity_upload_body.*

class UploadBodyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_body)

        btn_upload.setOnClickListener {
            //  上传资料

        }
    }
}
