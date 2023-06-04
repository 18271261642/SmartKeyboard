package com.app.smartkeyboard.second

import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.TitleBarFragment

/**
 * 数据页面
 */
class MenuDataFragment : TitleBarFragment<SecondHomeActivity>()
{

    companion object{

        fun getInstance() : MenuDataFragment{
            return MenuDataFragment()
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_menu_data_layout
    }

    override fun initView() {

    }

    override fun initData() {

    }

}