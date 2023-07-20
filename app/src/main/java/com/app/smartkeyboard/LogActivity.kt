package com.app.smartkeyboard

import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels

import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.viewmodel.KeyBoardViewModel
import com.google.gson.Gson



/**
 * Created by Admin
 *Date 2023/4/20
 */
class LogActivity : AppActivity() {

    private var logTv : TextView ?= null

    private var clearBtn : Button ?= null

    private var updateLogTv : TextView ?= null


    private val viewModel by viewModels<KeyBoardViewModel>()
    override fun getLayoutId(): Int {
        return R.layout.activity_log_layout
    }

    override fun initView() {
        updateLogTv = findViewById(R.id.updateLogTv)
        logTv = findViewById(R.id.logTv)
        clearBtn = findViewById(R.id.clearBtn)
        clearBtn?.setOnClickListener{
            BaseApplication.getBaseApplication().logStr = "--"
            BaseApplication.getBaseApplication().clearLog()
            logTv?.text = ""
            updateLogTv?.text = ""
        }

        findViewById<Button>(R.id.requestBtn).setOnClickListener {
           // request()
            viewModel.checkRequest(this)
        }
    }



    private fun request(){

        viewModel.firmwareData.observe(this){
            updateLogTv?.text = Gson().toJson(it)
        }
        viewModel.checkVersion(this,0)

    }

    override fun initData() {


       // val logStr = BaseApplication.getBaseApplication().bleOperate.log.toString()

        val logStr = BaseApplication.getBaseApplication().logStr

        logTv?.text = logStr

        val updateLog = BaseApplication.getBaseApplication().getAppLog()
        updateLogTv?.text = updateLog


        viewModel?.logData.observe(this){
            updateLogTv?.text = it
        }
    }
}