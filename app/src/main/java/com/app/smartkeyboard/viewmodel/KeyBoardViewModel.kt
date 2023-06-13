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
    var firmwareData = SingleLiveEvent<OtaBean>()


    //检查版本
    fun checkVersion(lifecycleOwner: LifecycleOwner,versionCode : Int){
        EasyHttp.get(lifecycleOwner).api("checkUpdate?firmwareVersionCode=$versionCode&productNumber=c003").request(object : OnHttpListener<String>{
            override fun onSucceed(result: String?) {
                val jsonObject = JSONObject(result)
                if(jsonObject.getInt("code") == 200){
                    val data = jsonObject.getJSONObject("data")
                    val firmware = data.getString("firmware")

                    if(!BikeUtils.isEmpty(firmware)){
                        val bean = GsonUtils.getGsonObject<OtaBean>(firmware)
                        Timber.e("------bean="+bean.toString())
                        firmwareData.postValue(bean)
                    }
                }
            }

            override fun onFail(e: Exception?) {
                e?.printStackTrace()
            }

        })
    }
}