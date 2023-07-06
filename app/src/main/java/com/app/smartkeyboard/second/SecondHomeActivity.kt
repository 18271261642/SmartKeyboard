package com.app.smartkeyboard.second

import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.action.AppFragment
import com.app.smartkeyboard.adapter.OnCommItemClickListener
import com.app.smartkeyboard.widget.HomeMenuView
import com.bonlala.base.FragmentPagerAdapter
import timber.log.Timber

/**
 * 键盘二代主页，三个底部菜单
 */
class SecondHomeActivity : AppActivity(){


    private val INTENT_KEY_IN_FRAGMENT_INDEX = "fragmentIndex"
    private val INTENT_KEY_IN_FRAGMENT_CLASS = "fragmentClass"

    private var mViewPager: ViewPager? = null

    private var mPagerAdapter: FragmentPagerAdapter<AppFragment<*>>? = null

    private var secondHomeMenuView : HomeMenuView ?= null

    override fun getLayoutId(): Int {
        return R.layout.activity_second_home_layout
    }

    override fun initView() {
        mViewPager = findViewById(R.id.vp_home_pager)
        secondHomeMenuView = findViewById(R.id.secondHomeMenuView)

        secondHomeMenuView?.setOnItemClick(object :OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                switchFragment(position)
            }

        })

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
}