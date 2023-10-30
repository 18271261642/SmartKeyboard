package com.app.smartkeyboard.second

import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.TextView
import com.app.smartkeyboard.BaseApplication
import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.TitleBarFragment
import com.app.smartkeyboard.dialog.DeleteDeviceDialog
import com.app.smartkeyboard.utils.MmkvUtils
import com.hjq.shape.view.ShapeTextView

/**
 * 设备页面
 */
class MenuDeviceFragment : TitleBarFragment<SecondHomeActivity>(){

    //设备名称
    private var deviceDeviceNameTv : TextView ?= null


    companion object{

        fun getInstance() : MenuDeviceFragment{
            return MenuDeviceFragment()
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.fragment_menu_device_layout
    }

    override fun initView() {
        deviceDeviceNameTv = findViewById(R.id.deviceDeviceNameTv)
        findViewById<ShapeTextView>(R.id.deviceNotifyTv).setOnClickListener {
            startActivity(NotifyOpenActivity::class.java)
        }

        findViewById<ShapeTextView>(R.id.deviceUnBindTv).setOnClickListener {
            showUnBindDialog()
        }
        //关于设备
        findViewById<ShapeTextView>(R.id.deviceAboutTv).setOnClickListener {

        }
    }

    override fun initData() {

    }


    override fun onFragmentResume(first: Boolean) {
        super.onFragmentResume(first)
        deviceDeviceNameTv?.text = MmkvUtils.getConnDeviceName()
    }


    private fun showUnBindDialog(){
        val dialog = DeleteDeviceDialog(attachActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setOnCommClickListener { position ->
            dialog.dismiss()
            if (position == 0x01) {   //解绑
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                MmkvUtils.saveConnDeviceName("")
                MmkvUtils.saveConnDeviceMac("")
                attachActivity.showIsAddDevice()
            }
        }

        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = metrics2.widthPixels

        windowLayout?.width = widthW
        windowLayout?.gravity = Gravity.BOTTOM
        window?.attributes = windowLayout
    }
}