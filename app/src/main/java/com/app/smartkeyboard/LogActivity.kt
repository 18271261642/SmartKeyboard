package com.app.smartkeyboard

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.viewmodel.KeyBoardViewModel
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.lang.Exception
import java.lang.Thread.sleep


/**
 * Created by Admin
 *Date 2023/4/20
 */
class LogActivity : AppActivity() {

    private var logTv: TextView? = null

    private var clearBtn: Button? = null

    private var updateLogTv: TextView? = null


    private val viewModel by viewModels<KeyBoardViewModel>()

    private val stringBuffer = StringBuffer()

    private var pingLogTv :TextView ?= null


    private val handlers : Handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            pingLogTv?.text = stringBuffer.toString()
            Timber.e("-------sss="+stringBuffer.toString())


        }
    }
    override fun getLayoutId(): Int {
        return R.layout.activity_log_layout
    }

    override fun initView() {
        pingLogTv = findViewById(R.id.pingLogTv)
        updateLogTv = findViewById(R.id.updateLogTv)
        logTv = findViewById(R.id.logTv)
        clearBtn = findViewById(R.id.clearBtn)
        clearBtn?.setOnClickListener {
            BaseApplication.getBaseApplication().logStr = "--"
            BaseApplication.getBaseApplication().clearLog()
            logTv?.text = ""
            updateLogTv?.text = ""
            startActivity(PingT::class.java)
        }

        findViewById<Button>(R.id.requestBtn).setOnClickListener {
            // request()
            viewModel.checkRequest(this)
        }
    }


    private fun request() {

        viewModel.firmwareData.observe(this) {
            updateLogTv?.text = Gson().toJson(it)
        }
        viewModel.checkVersion(this, 0)

    }

    override fun initData() {


        // val logStr = BaseApplication.getBaseApplication().bleOperate.log.toString()

        val logStr = BaseApplication.getBaseApplication().logStr

        logTv?.text = logStr

        val updateLog = BaseApplication.getBaseApplication().getAppLog()
        updateLogTv?.text = updateLog


        viewModel?.logData.observe(this) {
            updateLogTv?.text = it
        }

        stringBuffer.delete(0,stringBuffer.length)
        viewModel.setAppData(this, this)
    }


    private var process : Process?=null
    private fun pingW() {
        try {
            val address = "34.66.19.66"
            process = Runtime.getRuntime().exec("ping $address")
            val r = InputStreamReader(process?.inputStream)
            val returnData = LineNumberReader(r)
            Timber.e("--returnData="+(returnData==null))
            var returnMsg = ""
            var line = ""
            while (returnData.readLine().also { line = it } != null) {
                println(line)
                stringBuffer.append(line+"\n")
                returnMsg += line
                Timber.e("----------dfdasfsafsd="+line)
                handlers.sendEmptyMessageDelayed(0x00,300)
            }

            if (returnMsg.indexOf("100% loss") != -1) {
                System.out.println("与 $address 连接不畅通.")
                stringBuffer.append("与 $address 连接不畅通."+"\n")
            } else {
                System.out.println("与 $address 连接畅通.")
                stringBuffer.append("与 $address 连接畅通."+"\n")
            }

        }catch (e : Exception){
            e.printStackTrace()
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        process?.destroy()
    }

}