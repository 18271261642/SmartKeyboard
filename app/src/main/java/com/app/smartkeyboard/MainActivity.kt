package com.app.smartkeyboard


import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.NotificationManagerCompat
import com.app.smartkeyboard.action.ActivityManager
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.ble.ConnStatus
import com.app.smartkeyboard.dialog.NoticeDialog
import com.app.smartkeyboard.dialog.ShowPrivacyDialogView
import com.app.smartkeyboard.utils.MmkvUtils
import com.hjq.toast.ToastUtils

/**
 * 首页面
 */
class MainActivity : AppActivity() {

    //记事本
    private var homeNotebookLayout: FrameLayout? = null

    //键盘页面
    private var homeKeyboardLayout: FrameLayout? = null

    private var homeDialLayout: FrameLayout? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        homeNotebookLayout = findViewById(R.id.homeNotebookLayout)
        homeKeyboardLayout = findViewById(R.id.homeKeyboardLayout)
        homeDialLayout = findViewById(R.id.homeDialLayout)

        setOnClickListener(homeNotebookLayout, homeKeyboardLayout, homeDialLayout)
    }

    override fun initData() {
        //判断是否需要显示隐私政策，第一次打开需要隐私政策
        val isFirstOpen = MmkvUtils.getPrivacy()
        if (!isFirstOpen) {
            showPrivacyDialog()
        }else{
            showOpenNotifyDialog()
        }
    }

    //显示隐私弹窗
    private fun showPrivacyDialog() {
        val dialog =
            ShowPrivacyDialogView(this, com.bonlala.base.R.style.BaseDialogTheme, this@MainActivity)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setOnPrivacyClickListener(object : ShowPrivacyDialogView.OnPrivacyClickListener {
            override fun onCancelClick() {
                dialog.dismiss()
                MmkvUtils.setIsAgreePrivacy(false)
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                BaseApplication.getBaseApplication().connStatus = ConnStatus.NOT_CONNECTED
                ActivityManager.getInstance().finishAllActivities()
                finish()
            }

            override fun onConfirmClick() {
                dialog.dismiss()
                MmkvUtils.setIsAgreePrivacy(true)
                showOpenNotifyDialog()
            }

        })
    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when (id) {
            //表盘
            R.id.homeDialLayout -> {
                startActivity(DialHomeActivity::class.java)
            }
            //记事本
            R.id.homeNotebookLayout -> {
                startActivity(NotebookActivity::class.java)
            }

            //键盘
            R.id.homeKeyboardLayout -> {
                startActivity(BleKeyboardActivity::class.java)
            }
        }
    }

    private var mExitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // 过滤按键动作
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                mExitTime = System.currentTimeMillis()
                ToastUtils.show(resources.getString(R.string.string_double_click_exit))
                return true
            } else {
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                BaseApplication.getBaseApplication().connStatus = ConnStatus.NOT_CONNECTED
                ActivityManager.getInstance().finishAllActivities()
                finish()
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    //显示打开通知权限弹窗
    private fun showOpenNotifyDialog() {
        //判断通知权限是否打开了
        val isOpen = hasNotificationListenPermission(this)
        if(isOpen)
            return




        val dialog = NoticeDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setOnDialogClickListener { position ->
            if (position == 0x00) {
                dialog.dismiss()
                startToNotificationListenSetting(this@MainActivity)
            }
            if (position == 0x01) {
                dialog.dismiss()
            }
        }
    }


    /**
     * 跳转到通知内容读取权限设置
     *
     * @param context
     */
    private fun startToNotificationListenSetting(context: Activity) {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivityForResult(intent, 0x08)
            //context.startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val cn = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                )
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    /**
     * 有通知/监听读取权限
     * 迁移到SNNotificationListener依赖库
     *
     * @param context
     * @return
     */
    private fun hasNotificationListenPermission(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return !packageNames.isEmpty() && packageNames.contains(context.packageName)
    }
}