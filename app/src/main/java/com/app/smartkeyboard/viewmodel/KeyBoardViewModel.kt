package com.app.smartkeyboard.viewmodel

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.app.smartkeyboard.bean.OtaBean
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.GsonUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.kymjs.rxvolley.RxVolley
import org.json.JSONObject
import timber.log.Timber
import java.util.Observable
import java.util.concurrent.Flow.Subscriber


class KeyBoardViewModel : ViewModel() {


    //升级的内容
    var firmwareData = SingleLiveEvent<OtaBean?>()


    //检查版本
    fun checkVersion(lifecycleOwner: LifecycleOwner,versionCode : Int){
        getTest()


//
//        RxVolley.get("http://www.baidu.com",object : HttpCallback(){
//            override fun onPreStart() {
//                super.onPreStart()
//                Timber.e("-------")
//            }
//
//            override fun onPreHttp() {
//                super.onPreHttp()
//                Timber.e("-------")
//            }
//
//            override fun onSuccessInAsync(t: ByteArray?) {
//                super.onSuccessInAsync(t)
//                Timber.e("-------")
//            }
//
//            override fun onSuccess(t: String?) {
//                super.onSuccess(t)
//                Timber.e("-------onSuccess="+t)
//            }
//
//            override fun onSuccess(headers: MutableMap<String, String>?, t: ByteArray?) {
//                super.onSuccess(headers, t)
//                Timber.e("-------onSuccess="+headers?.toString()+" "+ t?.let { String(it) })
//            }
//
//            override fun onSuccess(headers: MutableMap<String, String>?, bitmap: Bitmap?) {
//                super.onSuccess(headers, bitmap)
//                Timber.e("-------onSuccess="+headers?.toString())
//            }
//
//            override fun onFailure(errorNo: Int, strMsg: String?) {
//                super.onFailure(errorNo, strMsg)
//                Timber.e("---onFailure----=$errorNo $strMsg")
//            }
//
//            @SuppressLint("BinaryOperationInTimber")
//            override fun onFailure(error: VolleyError?) {
//                super.onFailure(error)
//                Timber.e("---onFailure----="+error?.message)
//            }
//
//            override fun onFinish() {
//                super.onFinish()
//                Timber.e("---onFinish----")
//            }
//        })



        EasyHttp.get(lifecycleOwner).api("checkUpdate?firmwareVersionCode=$versionCode&productNumber=c003").request(object : OnHttpListener<String>{
            override fun onHttpSuccess(result: String?) {
                val jsonObject = JSONObject(result)
                if(jsonObject.getInt("code") == 200){
                    val data = jsonObject.getJSONObject("data")
                    val firmware = data.getString("firmware")

                    if(!BikeUtils.isEmpty(firmware)){
                        var bean = GsonUtils.getGsonObject<OtaBean>(firmware)
                        if(bean == null){
                            bean = OtaBean()
                            bean.isError=true
                            bean.setErrorMsg("back null")
                        }else{
                            bean.isError = false
                        }
                        Timber.e("------bean="+bean.toString())
                        firmwareData.postValue(bean)
                    }else{
                        val bean = OtaBean()
                        bean.isError=true
                        bean.setErrorMsg("back null")
                        firmwareData.postValue(bean)
                    }
                }
            }

            override fun onHttpFail(e: Exception?) {
                val bean = OtaBean()
                bean.isError=true
                bean.setErrorMsg(e?.message+"\n"+e?.printStackTrace())
                firmwareData.postValue(bean)
            }


        })
    }

    private fun getTest() {
        


//        val params = HttpParams()
//
////http header, optional parameters
//        params.putHeaders("cookie", "your cookie")
//        params.putHeaders("User-Agent", "rxvolley")
//
////request parameters
////        params.put("name", "kymjs")
////        params.put("age", "18")
//        val callBack: HttpCallback = object : HttpCallback() {
//            override fun onSuccess(t: String) {
//                Timber.e("---succ="+t)
//            }
//            override fun onFailure(errorNo: Int, strMsg: String) {
//                Timber.e("---onFailure="+errorNo+" "+strMsg)
//            }
//        }
//        RxVolley.Builder()
//            .url("https://wuquedistribution.com:12349/checkUpdate?firmwareVersionCode=1&productNumber=c003")
//            .httpMethod(RxVolley.Method.GET) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
//            .cacheTime(6) //default: get 5min, post 0min
//            .contentType(RxVolley.ContentType.FORM) //default FORM or JSON
//            .params(params)
//            .shouldCache(true) //default: get true, post false
//            .callback(callBack)
//            .encoding("UTF-8") //default
//            .doTask()

    }

}