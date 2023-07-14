package com.app.smartkeyboard

import android.widget.Button
import android.widget.TextView
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.bean.OtaBean
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.GsonUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

/**
 * Created by Admin
 *Date 2023/4/20
 */
class LogActivity : AppActivity() {

    private var logTv : TextView ?= null

    private var clearBtn : Button ?= null

    private var updateLogTv : TextView ?= null


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
            request()
        }
    }



    private fun request(){
        EasyHttp.get(this).api("checkUpdate?firmwareVersionCode=320&productNumber=c003").request(object :
            OnHttpListener<String> {

            override fun onHttpSuccess(result: String?) {
                logTv?.text = result
            }

            override fun onHttpFail(e: Exception?) {
                Timber.e("----e="+e?.printStackTrace()+"\n"+e?.fillInStackTrace()+"\n"+e?.localizedMessage)
                logTv?.text = e?.message
            }

        })
    }

    override fun initData() {
       // val logStr = BaseApplication.getBaseApplication().bleOperate.log.toString()

        val logStr = BaseApplication.getBaseApplication().logStr

        logTv?.text = logStr

        val updateLog = BaseApplication.getBaseApplication().getAppLog()
        updateLogTv?.text = updateLog
    }
}