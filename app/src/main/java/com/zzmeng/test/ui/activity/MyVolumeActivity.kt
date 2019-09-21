package com.zzmeng.test.ui.activity

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.core.content.getSystemService
import com.zzmeng.test.R
import kotlinx.android.synthetic.main.activity_my_volume.*

class MyVolumeActivity : AppCompatActivity() {

    lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_volume)

        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_SHOW_UI)
            }
        })
        btn_add.setOnClickListener {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        }

        btn_remove.setOnClickListener {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        }

        initUI()
    }

    private fun initUI() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        Log.d("cz", "maxVolume:${audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)},  " +
                "currentVolume：${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)}")
        seekbar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
}
