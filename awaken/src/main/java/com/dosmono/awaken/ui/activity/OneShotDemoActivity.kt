package com.dosmono.awaken.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.dosmono.awaken.R
import com.dosmono.awaken.util.JsonParser
import com.iflytek.cloud.*
import com.iflytek.cloud.util.ResourceUtil
import kotlinx.android.synthetic.main.activity_one_shot_demo.*
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class OneShotDemoActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "ivw"
    private var mToast: Toast? = null
    private var textView: TextView? = null
    // 语音唤醒对象
    private var mIvw: VoiceWakeuper? = null
    // 语音识别对象
    private var mAsr: SpeechRecognizer? = null
    // 唤醒结果内容
    private var resultString: String? = null
    // 识别结果内容
    private var recoString: String? = null
    // 设置门限值 ： 门限值越低越容易被唤醒
    private var tvThresh: TextView? = null
    private var seekbarThresh: SeekBar? = null
    private val MAX = 3000
    private val MIN = 0
    private var curThresh = 1450
    private val threshStr = "门限值："
    // 云端语法文件
    private var mCloudGrammar: String? = null
    // 云端语法id
    private var mCloudGrammarID: String? = null
    // 本地语法id
    private var mLocalGrammarID: String? = null
    // 本地语法文件
    private var mLocalGrammar: String? = null
    // 本地语法构建路径
    private val grmPath = Environment.getExternalStorageDirectory().absolutePath + "/msc/test"
    // 引擎类型
    private var mEngineType = SpeechConstant.TYPE_CLOUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_demo)

        initUI()

        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(this, null)
        // 初始化识别对象---唤醒+识别,用来构建语法
        mAsr = SpeechRecognizer.createRecognizer(this, null)
        // 初始化语法文件
        mCloudGrammar = readFile(this, "wake_grammar_sample.abnf", "utf-8")
        mLocalGrammar = readFile(this, "wake.bnf", "utf-8")
    }

    private fun initUI() {
        btn_oneshot.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
        btn_grammar.setOnClickListener(this)
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
        //选择云端or本地
        val group = this.findViewById(R.id.radioGroup) as RadioGroup
        group.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioCloud) {
                mEngineType = SpeechConstant.TYPE_CLOUD
            } else if (checkedId == R.id.radioLocal) {
                mEngineType = SpeechConstant.TYPE_LOCAL
            }
        }
    }

    internal var grammarListener: GrammarListener = GrammarListener { grammarId, error ->
        if (error == null) {
            if (mEngineType == SpeechConstant.TYPE_CLOUD) {
                mCloudGrammarID = grammarId
            } else {
                mLocalGrammarID = grammarId
            }
            showTip("语法构建成功：$grammarId")
        } else {
            showTip("语法构建失败,错误码：" + error.errorCode + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_oneshot -> {
                // 非空判断，防止因空指针使程序崩溃
                mIvw = VoiceWakeuper.getWakeuper()
                if (mIvw != null) {
                    resultString = ""
                    recoString = ""
                    txt_show_msg.text = resultString

                    val resPath = ResourceUtil.generateResourcePath(
                        this,
                        ResourceUtil.RESOURCE_TYPE.assets,
                        "ivw/5d57ae5d.jet"
                    )
                    // 清空参数
                    mIvw!!.setParameter(SpeechConstant.PARAMS, null)
                    // 设置识别引擎
                    mIvw!!.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
                    // 设置唤醒资源路径
                    mIvw!!.setParameter(ResourceUtil.IVW_RES_PATH, resPath)
                    /**
                     * 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
                     * 示例demo默认设置第一个唤醒词，建议开发者根据定制资源中唤醒词个数进行设置
                     */
                    mIvw!!.setParameter(SpeechConstant.IVW_THRESHOLD, "0:$curThresh")
                    // 设置唤醒+识别模式
                    mIvw!!.setParameter(SpeechConstant.IVW_SST, "oneshot")
                    // 设置返回结果格式
                    mIvw!!.setParameter(SpeechConstant.RESULT_TYPE, "json")
                    //
                    //				mIvw.setParameter(SpeechConstant.IVW_SHOT_WORD, "0");

                    // 设置唤醒录音保存路径，保存最近一分钟的音频
                    mIvw!!.setParameter(
                        SpeechConstant.IVW_AUDIO_PATH,
                        Environment.getExternalStorageDirectory().path + "/msc/ivw.wav"
                    )
                    mIvw!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")

                    if (mEngineType == SpeechConstant.TYPE_CLOUD) {
                        if (!TextUtils.isEmpty(mCloudGrammarID)) {
                            // 设置云端识别使用的语法id
                            mIvw!!.setParameter(
                                SpeechConstant.CLOUD_GRAMMAR,
                                mCloudGrammarID
                            )
                            mIvw!!.startListening(mWakeuperListener)
                        } else {
                            showTip("请先构建语法")
                        }
                    } else {
                        if (!TextUtils.isEmpty(mLocalGrammarID)) {
                            // 设置本地识别资源
                            mIvw!!.setParameter(
                                ResourceUtil.ASR_RES_PATH,
                                getResourcePath()
                            )
                            // 设置语法构建路径
                            mIvw!!.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath)
                            // 设置本地识别使用语法id
                            mIvw!!.setParameter(
                                SpeechConstant.LOCAL_GRAMMAR,
                                mLocalGrammarID
                            )
                            mIvw!!.startListening(mWakeuperListener)
                        } else {
                            showTip("请先构建语法")
                        }
                    }

                } else {
                    showTip("唤醒未初始化")
                }
            }

            R.id.btn_grammar -> {
                var ret = 0
                if (mEngineType == SpeechConstant.TYPE_CLOUD) {
                    // 设置参数
                    mAsr?.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
                    mAsr?.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
                    // 开始构建语法
                    ret = mAsr?.buildGrammar("abnf", mCloudGrammar, grammarListener) ?: 0
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("语法构建失败,错误码：$ret,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
                    }
                } else {
                    mAsr?.setParameter(SpeechConstant.PARAMS, null)
                    mAsr?.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
                    // 设置引擎类型
                    mAsr?.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
                    // 设置语法构建路径
                    mAsr?.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath)
                    // 设置资源路径
                    mAsr?.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath())
                    ret = mAsr?.buildGrammar("bnf", mLocalGrammar, grammarListener) ?: 0
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("语法构建失败,错误码：$ret,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
                    }
                }
            }

            R.id.btn_stop -> {
                mIvw = VoiceWakeuper.getWakeuper()
                if (mIvw != null) {
                    mIvw!!.stopListening()
                } else {
                    showTip("唤醒未初始化")
                }
            }

            else -> {
            }
        }
    }

    private val mWakeuperListener = object : WakeuperListener {

        override fun onResult(result: WakeuperResult) {
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
        }

        override fun onBeginOfSpeech() {
            showTip("开始说话")
        }

        override fun onEvent(eventType: Int, isLast: Int, arg2: Int, obj: Bundle) {
            Log.d(TAG, "eventType:" + eventType + "arg1:" + isLast + "arg2:" + arg2)
            // 识别结果
            if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
                val reslut = obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT) as RecognizerResult?
                recoString += JsonParser.parseGrammarResult(reslut?.resultString)
                txt_show_msg.text = recoString
            }
        }

        override fun onVolumeChanged(volume: Int) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * 读取asset目录下文件。
     *
     * @return content
     */
    fun readFile(mContext: Context, file: String, code: String): String {
        var len = 0
        var buf: ByteArray? = null
        var result = ""
        try {
            val `in` = mContext.assets.open(file)
            len = `in`.available()
            buf = ByteArray(len)
            `in`.read(buf, 0, len)

            result = String(buf, Charset.forName(code))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    // 获取识别资源路径
    private fun getResourcePath(): String {
        val tempBuffer = StringBuffer()
        // 识别通用资源
        tempBuffer.append(
            ResourceUtil.generateResourcePath(
                this,
                ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"
            )
        )
        return tempBuffer.toString()
    }

    private fun showTip(str: String) {
        mToast?.setText(str)
        mToast?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mIvw = VoiceWakeuper.getWakeuper()
        if (mIvw != null) {
            mIvw!!.destroy()
        } else {
            showTip("唤醒未初始化")
        }
    }

}
