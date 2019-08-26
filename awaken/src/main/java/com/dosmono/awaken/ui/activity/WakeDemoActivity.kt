package com.dosmono.awaken.ui.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.dosmono.awaken.R
import com.iflytek.cloud.*
import com.iflytek.cloud.util.ResourceUtil
import kotlinx.android.synthetic.main.activity_wake_demo.*
import org.json.JSONException
import org.json.JSONObject

class WakeDemoActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "ivw"
    private var mToast: Toast? = null
    private var textView: TextView? = null
    // 语音唤醒对象
    private var mIvw: VoiceWakeuper? = null
    // 唤醒结果内容
    private var resultString: String? = null

    // 设置门限值 ： 门限值越低越容易被唤醒
    private var tvThresh: TextView? = null
    private var seekbarThresh: SeekBar? = null
    private val MAX = 3000
    private val MIN = 0
    private var curThresh = 1450
    private val threshStr = "门限值："
    private val keep_alive = "1"
    private var ivwNetMode = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wake_demo)

        initUi()
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(this) { p0 -> Log.e(TAG, "onInit:$p0") }
    }

    @SuppressLint("ShowToast", "SetTextI18n")
    private fun initUi() {
        btn_start.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        seekBar_thresh.max = MAX - MIN
        seekBar_thresh.progress = curThresh
        txt_thresh.text = threshStr + curThresh
        seekBar_thresh.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStopTrackingTouch(arg0: SeekBar) {}

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
                curThresh = seekBar_thresh.progress + MIN
                txt_thresh.text = threshStr + curThresh
            }
        })

        ivw_net_mode.setOnCheckedChangeListener { _, arg1 ->
            /**
             * 闭环优化网络模式有三种：
             * 模式0：关闭闭环优化功能
             *
             * 模式1：开启闭环优化功能，允许上传优化数据。需开发者自行管理优化资源。
             * sdk提供相应的查询和下载接口，请开发者参考API文档，具体使用请参考本示例
             * queryResource及downloadResource方法；
             *
             * 模式2：开启闭环优化功能，允许上传优化数据及启动唤醒时进行资源查询下载；
             * 本示例为方便开发者使用仅展示模式0和模式2；
             */
            /**
             * 闭环优化网络模式有三种：
             * 模式0：关闭闭环优化功能
             *
             * 模式1：开启闭环优化功能，允许上传优化数据。需开发者自行管理优化资源。
             * sdk提供相应的查询和下载接口，请开发者参考API文档，具体使用请参考本示例
             * queryResource及downloadResource方法；
             *
             * 模式2：开启闭环优化功能，允许上传优化数据及启动唤醒时进行资源查询下载；
             * 本示例为方便开发者使用仅展示模式0和模式2；
             */
            when (arg1) {
                R.id.mode_close -> ivwNetMode = "0"
                R.id.mode_open -> ivwNetMode = "1"
                else -> {
                }
            }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_start -> {
                //非空判断，防止因空指针使程序崩溃
                mIvw = VoiceWakeuper.getWakeuper()
                if (mIvw != null) {
                    setRadioEnable(false)
                    resultString = ""
                    txt_thresh.text = resultString
                    // 清空参数
                    mIvw!!.setParameter(SpeechConstant.PARAMS, null)
                    // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
                    mIvw!!.setParameter(SpeechConstant.IVW_THRESHOLD, "0:$curThresh")
                    // 设置唤醒模式
                    mIvw!!.setParameter(SpeechConstant.IVW_SST, "wakeup")
                    // 设置持续进行唤醒
                    mIvw!!.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive)
                    // 设置闭环优化网络模式
                    mIvw!!.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode)
                    // 设置唤醒资源路径
                    mIvw!!.setParameter(SpeechConstant.IVW_RES_PATH, getResource())
                    // 设置唤醒录音保存路径，保存最近一分钟的音频
                    mIvw!!.setParameter(
                        SpeechConstant.IVW_AUDIO_PATH,
                        Environment.getExternalStorageDirectory().path + "/msc/ivw.wav"
                    )
                    mIvw!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
                    // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
                    //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );

                    // 启动唤醒
                    mIvw!!.startListening(mWakeuperListener)
                } else {
                    showTip("唤醒未初始化")
                }
            }
            R.id.btn_stop -> {
                mIvw?.stopListening()
                setRadioEnable(true)
            }
            else -> {
            }
        }
    }

    private val mWakeuperListener = object : WakeuperListener {

        override fun onResult(result: WakeuperResult) {
            Log.d(TAG, "onResult")
            if (!"1".equals(keep_alive, ignoreCase = true)) {
                setRadioEnable(true)
            }
            try {
                val text = result.resultString
                val `object`: JSONObject
                `object` = JSONObject(text)
                val buffer = StringBuffer()
                buffer.append("【RAW】 $text")
                buffer.append("\n")
                buffer.append("【操作类型】" + `object`.optString("sst"))
                buffer.append("\n")
                buffer.append("【唤醒词id】" + `object`.optString("id"))
                buffer.append("\n")
                buffer.append("【得分】" + `object`.optString("score"))
                buffer.append("\n")
                buffer.append("【前端点】" + `object`.optString("bos"))
                buffer.append("\n")
                buffer.append("【尾端点】" + `object`.optString("eos"))
                resultString = buffer.toString()
            } catch (e: JSONException) {
                resultString = "结果解析出错"
                e.printStackTrace()
            }

            txt_show_msg.text = resultString
        }

        override fun onError(error: SpeechError) {
            showTip(error.getPlainDescription(true))
            setRadioEnable(true)
        }

        override fun onBeginOfSpeech() {}

        override fun onEvent(eventType: Int, isLast: Int, arg2: Int, obj: Bundle) {
            when (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                SpeechEvent.EVENT_RECORD_DATA -> {
                    val audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA)
                    Log.i(TAG, "ivw audio length: " + audio!!.size)
                }
            }
        }

        override fun onVolumeChanged(volume: Int) {

        }
    }

    private fun getResource(): String {
        val resPath = ResourceUtil.generateResourcePath(this@WakeDemoActivity, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + "5d57ae5d" + ".jet")
        Log.d(TAG, "resPath: $resPath")
        return resPath
    }

    private fun showTip(str: String) {
        runOnUiThread {
            mToast?.setText(str)
            mToast?.show()
        }
    }

    private fun setRadioEnable(enabled: Boolean) {
        runOnUiThread {
            ivw_net_mode.isEnabled = enabled
            btn_start.isEnabled = enabled
            seekBar_thresh.isEnabled = enabled
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy WakeDemo")
        // 销毁合成对象
        mIvw = VoiceWakeuper.getWakeuper()
        if (mIvw != null) {
            mIvw!!.destroy()
        }
    }
}
