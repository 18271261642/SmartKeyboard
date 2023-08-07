package com.app.smartkeyboard.second

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.app.smartkeyboard.BaseApplication
import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.ActivityManager
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.action.AppFragment
import com.app.smartkeyboard.adapter.OnCommItemClickListener
import com.app.smartkeyboard.ble.ConnStatus
import com.app.smartkeyboard.utils.BikeUtils
import com.app.smartkeyboard.utils.MmkvUtils
import com.app.smartkeyboard.widget.HomeMenuView
import com.bonlala.base.FragmentPagerAdapter
import com.hjq.shape.layout.ShapeLinearLayout
import com.hjq.toast.ToastUtils
import timber.log.Timber

/**
 * 键盘二代主页，三个底部菜单
 */
class SecondHomeActivity : AppActivity(){


    private var scanHolderLayout : LinearLayout ?= null
    private var dataAddLayout : ShapeLinearLayout?= null

    private val INTENT_KEY_IN_FRAGMENT_INDEX = "fragmentIndex"
    private val INTENT_KEY_IN_FRAGMENT_CLASS = "fragmentClass"

    private var mViewPager: ViewPager? = null

    private var mPagerAdapter: FragmentPagerAdapter<AppFragment<*>>? = null

    private var secondHomeMenuView : HomeMenuView ?= null

    override fun getLayoutId(): Int {
        return R.layout.activity_second_home_layout
    }

    override fun initView() {
        dataAddLayout = findViewById(R.id.dataAddLayout)
        mViewPager = findViewById(R.id.vp_home_pager)
        secondHomeMenuView = findViewById(R.id.secondHomeMenuView)
        scanHolderLayout = findViewById(R.id.scanHolderLayout)

        secondHomeMenuView?.setOnItemClick(object :OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                switchFragment(position)
            }

        })

        dataAddLayout?.setOnClickListener { startActivity(SecondScanActivity::class.java) }

    }

    override fun initData() {
        mPagerAdapter = FragmentPagerAdapter(this)
        mPagerAdapter?.addFragment(MenuDataFragment.getInstance())
        mPagerAdapter?.addFragment(MenuSettingFragment.getInstance())
        mPagerAdapter?.addFragment(MenuDeviceFragment.getInstance())
        mViewPager?.adapter = mPagerAdapter

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        switchFragment(mPagerAdapter!!.getFragmentIndex(getSerializable(INTENT_KEY_IN_FRAGMENT_CLASS)))
    }


    override fun onResume() {
        super.onResume()
        showIsAddDevice()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前 Fragment 索引位置
        outState.putInt(INTENT_KEY_IN_FRAGMENT_INDEX, mViewPager!!.currentItem)
    }


    private fun switchFragment(fragmentIndex: Int) {
        if (fragmentIndex == -1) {
            return
        }

        Timber.e("-------swww=" + fragmentIndex)

        when (fragmentIndex) {
            0, 1, 2 -> {
                mViewPager!!.currentItem = fragmentIndex

            }
            else -> {}
        }
    }

    //是否显示添加设备
     fun showIsAddDevice(){
        //是否有连接过
        val isMac = MmkvUtils.getConnDeviceMac()
        Timber.e("-----isMac="+isMac)
        scanHolderLayout?.visibility = if(BikeUtils.isEmpty(isMac)) View.VISIBLE else View.GONE
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
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}