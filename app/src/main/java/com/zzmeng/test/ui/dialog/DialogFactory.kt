package com.zzmeng.test.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.zzmeng.test.R


/**
 *  @项目名：  TestProject
 *  @创建者:   chenzhuaung
 *  @创建时间:  2019/8/19 10:11
 *  @描述：    DialogFactory
 */
object DialogFactory {

    /**
     * 显示AI Dialog
     */
    fun createAiDialog(context: Context): Dialog {
        val aiDialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_ai_layout, null)
        view.findViewById<TextView>(R.id.tv_dismiss).setOnClickListener {
            aiDialog.dismiss()
        }
        aiDialog.setContentView(view)
        aiDialog.setCancelable(false)
        aiDialog.setCanceledOnTouchOutside(false)
        val window = aiDialog.window
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        window.setBackgroundDrawable(ColorDrawable())
        return aiDialog
    }
}