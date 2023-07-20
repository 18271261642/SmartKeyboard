package com.app.smartkeyboard.viewmodel


import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.app.smartkeyboard.BaseApplication
import com.app.smartkeyboard.bean.OtaBean
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.GsonUtils
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tencent.bugly.crashreport.CrashReport
import okhttp3.Call
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit


class KeyBoardViewModel : ViewModel() {


    //升级的内容
    var firmwareData = SingleLiveEvent<OtaBean?>()


    //log
    var logData = SingleLiveEvent<String>()

    //检查版本
    fun checkVersion(lifecycleOwner: LifecycleOwner, versionCode: Int) {

        val stringRequest =
            StringRequest(BaseApplication.BASE_URL + "checkUpdate?firmwareVersionCode=$versionCode&productNumber=c003",
                { response ->
                    Timber.e("----response=" + response)
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getInt("code") == 200) {
                        val data = jsonObject.getJSONObject("data")
                        val firmware = data.getString("firmware")

                        if (!BikeUtils.isEmpty(firmware)) {
                            var bean = GsonUtils.getGsonObject<OtaBean>(firmware)
                            if (bean == null) {
                                bean = OtaBean()
                                bean.isError = true
                                bean.setErrorMsg("back null")
                            } else {
                                bean.isError = false
                            }
                            Timber.e("------bean=" + bean.toString())
                            firmwareData.postValue(bean)
                        } else {
                            val bean = OtaBean()
                            bean.isError = true
                            bean.setErrorMsg("back null")
                            firmwareData.postValue(bean)
                        }
                    }
                }
            ) { error ->
                Timber.e("----ErrorListener=" + error?.message)
                val bean = OtaBean()
                bean.isError = true
                bean.setErrorMsg(error?.message + "\n" + error?.printStackTrace())
                firmwareData.postValue(bean)

                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                CrashReport.postCatchedException(CusException(errorStr))

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)


    }

    val stringBuffer = StringBuffer()

    fun checkRequest(lifecycleOwner: LifecycleOwner) {
        stringBuffer.delete(0, stringBuffer.length)


        //volley
        val stringRequest =
            StringRequest(BaseApplication.BASE_URL + "checkUpdate?firmwareVersionCode=1&productNumber=c003",
                { response ->
                    Timber.e("----response=" + response)
                   stringBuffer.append("v keyboard:$response\n\n")
                    logData.postValue(stringBuffer.toString())
                }
            ) { error ->
                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                Timber.e("----ErrorListener=" + error?.message)
                stringBuffer.append("v keyboard error:"+errorStr+" " +error?.message+"\n\n")
                logData.postValue(stringBuffer.toString())

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)




        val httpUrl = "http://47.106.139.220:8089/find_app_update"

        val stringRequest3: StringRequest = object : StringRequest(
            Method.POST, httpUrl,
            Response.Listener<String> { response ->
                Timber.e("----response=" + response)
                stringBuffer.append("v aiHealth:$response\n\n")
                                      },
            Response.ErrorListener { error ->
                Timber.e("----ErrorListener=" + error?.message)
                stringBuffer.append("v aiHealth error:"+error?.message+"\n\n")
                logData.postValue(stringBuffer.toString())
            }) {
            override fun getParams(): Map<String, String>? {
                //在这里设置须要post的參数
                val map: MutableMap<String, String> = HashMap()
                map["appName"] = "aiHealth"
                map["versionCode"] ="1"
                map["type"] = "1"
                return map
            }
        }

        stringRequest3.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest3)





        //EasyHttp
        val config2 = EasyConfig.getInstance()
        config2.setServer("https://wuquedistribution.com:12349/")
        config2.into()
        EasyHttp.get(lifecycleOwner).api("checkUpdate?firmwareVersionCode=1&productNumber=c003")
            .request(
                object : OnHttpListener<String> {
                    override fun onHttpSuccess(result: String?) {
                        Timber.e("----key=" + result)
                        stringBuffer.append("e keyboard:$result\n\n")
                    }

                    override fun onHttpFail(e: Exception?) {
                        Timber.e("--onHttpFail--key=" + e?.message)
                        stringBuffer.append("e keyboard fail:" + e?.message + "\n\n")
                    }

                    override fun onHttpEnd(call: Call?) {
                        super.onHttpEnd(call)
                        logData.postValue(stringBuffer.toString())
                    }

                })


        //请求aiHealth
        val config = EasyConfig.getInstance()
        val m = HashMap<String, Any>()
        m["appName"] = "aiHealth"
        m["versionCode"] = 1
        m["type"] = 1
        config.params = m
        config.setServer("http://47.106.139.220:8089/")
        config.into()
        EasyHttp.post(lifecycleOwner).api("find_app_update")
            .request(object : OnHttpListener<String> {
                override fun onHttpSuccess(result: String?) {
                    Timber.e("--ai--key=" + result)
                    stringBuffer.append("e aiHealth:$result\n\n")
                }

                override fun onHttpFail(e: java.lang.Exception?) {
                    Timber.e("--ai--onHttpFail=" + e?.message)
                    stringBuffer.append("e aiHealth: fail:" + e?.message+"\n\n")
                }

                override fun onHttpEnd(call: Call?) {
                    super.onHttpEnd(call)
                    logData.postValue(stringBuffer.toString())
                }
            })


    }
}