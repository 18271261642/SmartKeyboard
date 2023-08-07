package com.app.smartkeyboard.second

import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.TitleBarFragment
import com.app.smartkeyboard.widget.SecondHomeTemperatureView

/**
 * 数据页面
 */
class MenuDataFragment : TitleBarFragment<SecondHomeActivity>()
{


    private var homeTempView : SecondHomeTemperatureView ?= null

    companion object{

        fun getInstance() : MenuDataFragment{
            return MenuDataFragment()
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_menu_data_layout
    }

    override fun initView() {
        homeTempView = findViewById(R.id.homeTempView)

    }

    override fun initData() {
        homeTempView?.setTemperatures("--","--","--")
    }

}