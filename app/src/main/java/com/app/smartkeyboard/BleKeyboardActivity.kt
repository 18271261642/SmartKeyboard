package com.app.smartkeyboard

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.ble.ConnStatus
import com.app.smartkeyboard.dialog.DialogScanDeviceView
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.BonlalaUtils
import com.app.smartkeyboard.utils.MmkvUtils
import com.blala.blalable.BleConstant
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.view.ShapeTextView
import timber.log.Timber

/**
 * 设置表盘页面
 * Created by Admin
 *Date 2023/1/12
 */
class BleKeyboardActivity : AppActivity(){

    //搜索的按钮
    private var scanReScanTv : ShapeTextView ?= null
    //空的
    private var scanEmptyLayout : LinearLayout ?= null

    //蓝牙的名称
    private var keyBoardNameTv : TextView ?= null
    //mac
    private var keyBoardMacTv : TextView ?= null
    //连接状态
    private var keyBoardStatusTv : ShapeTextView ?= null
    //解除绑定
    private var keyBoardUnBindTv : ShapeTextView ?= null
    //已经连接的布局
    private var keyBoardConnLayout : ShapeConstraintLayout ?= null

    //是否正在连接
    private var isConnecting = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        registerReceiver(broadcastReceiver,intentFilter)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_device_keyboard_layout
    }

    override fun initView() {
        scanReScanTv = findViewById(R.id.scanReScanTv)
        scanEmptyLayout = findViewById(R.id.scanEmptyLayout)

        keyBoardConnLayout = findViewById(R.id.keyBoardConnLayout)
        keyBoardNameTv = findViewById(R.id.keyBoardNameTv)
        keyBoardMacTv = findViewById(R.id.keyBoardMacTv)
        keyBoardStatusTv = findViewById(R.id.keyBoardStatusTv)
        keyBoardUnBindTv = findViewById(R.id.keyBoardUnBindTv)



    }

    override fun initData() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE)).request { permissions, all ->
                //verifyScanFun()
            }
        }

        scanReScanTv?.setOnClickListener {
            verifyScanFun(false)

        }

        //删除
        keyBoardUnBindTv?.setOnClickListener {
            dealDevice()
        }

        //重新连接
        keyBoardStatusTv?.setOnClickListener {
            reConnect()
        }
    }


    private fun reConnect(){
        if(BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED){
            return
        }

        verifyScanFun(true)
    }

    override fun onResume() {
        super.onResume()
        showDeviceStatus()
    }


    private fun dealDevice(){
        val alert = AlertDialog.Builder(this)
            .setTitle("提醒")
            .setMessage("是否解除绑定?")
            .setPositiveButton("确定"
            ) { p0, p1 ->
                p0?.dismiss()

                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                MmkvUtils.saveConnDeviceName(null)
                MmkvUtils.saveConnDeviceMac(null)

                showDeviceStatus()

            }
            .setNegativeButton("取消"
            ) { p0, p1 -> p0?.dismiss()
                p0?.dismiss()
            }
        alert.create().show()
    }

    //显示状态
    private fun showDeviceStatus(){
        //是否有绑定过
        val bleMac = MmkvUtils.getConnDeviceMac()
        val isBind = BikeUtils.isEmpty(bleMac)
        keyBoardConnLayout?.visibility = if(isBind) View.GONE else View.VISIBLE
        scanEmptyLayout?.visibility = if(isBind) View.VISIBLE else View.INVISIBLE


        //是否已连接
        if(!isBind){
            val bleName = MmkvUtils.getConnDeviceName()
            keyBoardNameTv?.text = resources.getString(R.string.string_name)+": "+bleName
            keyBoardMacTv?.text = "MAC: "+bleMac
            val isConn = BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED

            Timber.e("-----连接状态="+isConn)
            keyBoardStatusTv?.text = if(isConn) resources.getString(R.string.string_connected) else (if(isConnecting) resources.getString(R.string.string_connecting) else resources.getString(R.string.string_retry_conn))

        }

    }


    //判断是否有位置权限了，没有请求权限

    private fun verifyScanFun(isReconn : Boolean){

        //判断蓝牙是否开启
        if(!BikeUtils.isBleEnable(this)){
            BikeUtils.openBletooth(this)
            return
        }
        //判断权限
        val isPermission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if(!isPermission){
            XXPermissions.with(this).permission(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)).request { permissions, all ->
                verifyScanFun(isReconn)
            }
            // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x00)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE)).request { permissions, all ->
                //verifyScanFun()
            }
        }


        //判断蓝牙是否打开
        val isOpenBle = BonlalaUtils.isOpenBlue(this@BleKeyboardActivity)
        if(!isOpenBle){
            BonlalaUtils.openBluetooth(this)
            return
        }

        if(isReconn){
            val mac = MmkvUtils.getConnDeviceMac()
            if(BikeUtils.isEmpty(mac))
                return
            isConnecting = true
            keyBoardStatusTv?.text = resources.getString(R.string.string_connecting)
            BaseApplication.getBaseApplication().connStatusService.autoConnDevice(mac,false)
            keyBoardStatusTv?.text = resources.getString(R.string.string_connecting)
        }else{
            showScanDialog()
        }

    }


    //开始搜索，显示dialog
    private fun showScanDialog(){
        val dialog = DialogScanDeviceView(this@BleKeyboardActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.startScan()
        dialog.setOnDialogClickListener { position ->

            Timber.e("-----position="+position)
            if (position == 0x00) {   //显示进度条
                showDialog(resources.getString(R.string.string_connecting))
            }

            if (position == 0x01) {   //连接成功
                hideDialog()
                isConnecting = false
                showDeviceStatus()
            }
        }
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = (metrics2.widthPixels * 0.9f).toInt()
        val height : Int = (metrics2.heightPixels * 0.6f).toInt()
        windowLayout?.width = widthW
        windowLayout?.height = height
        window?.attributes = windowLayout

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        showScanDialog()
    }


    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            Timber.e("---------acdtion="+action)
            if(action == BleConstant.BLE_CONNECTED_ACTION){
                isConnecting = false
                showDeviceStatus()
            }
            if(action == BleConstant.BLE_DIS_CONNECT_ACTION){
                isConnecting = false
                showDeviceStatus()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
}