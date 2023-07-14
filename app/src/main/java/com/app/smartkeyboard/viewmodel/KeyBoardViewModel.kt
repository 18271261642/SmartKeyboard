package com.app.smartkeyboard.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.smartkeyboard.bean.OtaBean
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.GsonUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class KeyBoardViewModel : ViewModel() {


    //升级的内容
    var firmwareData = SingleLiveEvent<OtaBean?>()


    //检查版本
    fun checkVersion(lifecycleOwner: LifecycleOwner,versionCode : Int){
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
}