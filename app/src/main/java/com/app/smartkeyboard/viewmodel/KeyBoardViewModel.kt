package com.app.smartkeyboard.viewmodel


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy

import com.android.volley.toolbox.StringRequest
import com.app.smartkeyboard.BaseApplication
import com.app.smartkeyboard.bean.OtaBean
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.GsonUtils

import com.hjq.http.listener.OnHttpListener

import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit


class KeyBoardViewModel : ViewModel() {


    //升级的内容
    var firmwareData = SingleLiveEvent<OtaBean?>()


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
            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)


        /*  EasyHttp.get(lifecycleOwner).api("checkUpdate?firmwareVersionCode=$versionCode&productNumber=c003").reques*/(
                object : OnHttpListener<String> {
                    override fun onHttpSuccess(result: String?) {
                        val jsonObject = JSONObject(result)
                        if (jsonObject.getInt("code") == 200) {
                            val data = jsonObject.getJSONObject("data")
                            val firmware = data.getString("firmware")

                            if (!BikeUtils.isEmpty(firmware)) {
                                var bean = GsonUtils.getGsonObject<OtaBean>(firmware)
                                if (bean == null) {
                                    bean = OtaBean()
                                    bean?.isError = true
                                    bean?.errorMsg = "back null"
                                } else {
                                    bean?.isError = false
                                }
                                Timber.e("------bean=" + bean.toString())
                                firmwareData.postValue(bean)
                            } else {
                                val bean = OtaBean()
                                bean.isError = true
                                bean.errorMsg = "back null"
                                firmwareData.postValue(bean)
                            }
                        }
                    }

                    override fun onHttpFail(e: Exception?) {
                        val bean = OtaBean()
                        bean.isError = true
                        bean.errorMsg = e?.message + "\n" + e?.printStackTrace()
                        firmwareData.postValue(bean)
                    }


                })
    }

}