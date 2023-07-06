package com.app.smartkeyboard

import android.widget.Button
import android.widget.TextView
import com.app.smartkeyboard.action.AppActivity

/**
 * Created by Admin
 *Date 2023/4/20
 */
class LogActivity : AppActivity() {

    private var logTv : TextView ?= null

    private var clearBtn : Button ?= null



    override fun getLayoutId(): Int {
        return R.layout.activity_log_layout
    }

    override fun initView() {
        logTv = findViewById(R.id.logTv)
        clearBtn = findViewById(R.id.clearBtn)
        clearBtn?.setOnClickListener{
            BaseApplication.getBaseApplication().logStr = "--"
            logTv?.text = ""
        }
    }

    override fun initData() {
       // val logStr = BaseApplication.getBaseApplication().bleOperate.log.toString()

        val logStr = BaseApplication.getBaseApplication().logStr

        logTv?.text = logStr
    }
}