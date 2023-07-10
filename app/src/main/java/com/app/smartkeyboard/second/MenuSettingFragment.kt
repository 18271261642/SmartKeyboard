package com.app.smartkeyboard.second

import android.view.View
import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.TitleBarFragment
import com.app.smartkeyboard.widget.CheckButtonView


/**
 * 设备设置页面
 */
class MenuSettingFragment : TitleBarFragment<SecondHomeActivity>() {

    private var settingNoteLayout : CheckButtonView ?= null
    private var settingAlarmLayout : CheckButtonView ?= null


    companion object{

        fun getInstance():MenuSettingFragment{
            return MenuSettingFragment()
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.fragment_menu_setting_layout
    }

    override fun initView() {
        settingNoteLayout = findViewById(R.id.settingNoteLayout)
        settingAlarmLayout = findViewById(R.id.settingAlarmLayout)


        settingNoteLayout?.setOnClickListener(this)
        settingAlarmLayout?.setOnClickListener(this)
    }

    override fun initData() {

    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when(id){
            R.id.settingNoteLayout->{
                startActivity(NotePadActivity::class.java)
            }
            R.id.settingAlarmLayout->{
                startActivity(AlarmListActivity::class.java)
            }
        }
    }
}