package com.app.smartkeyboard.second

import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.utils.MmkvUtils
import com.bonlala.widget.view.SwitchButton

/**
 * 消息提醒
 */
class NotifyOpenActivity : AppActivity() {


    private var notifyDiscordSwitch : SwitchButton ?= null
    private var notifyQqSwitch : SwitchButton ?= null
    private var notifyWxSwitch : SwitchButton ?= null





    override fun getLayoutId(): Int {
        return R.layout.activity_msg_notify_layout
    }

    override fun initView() {
        notifyDiscordSwitch = findViewById(R.id.notifyDiscordSwitch)
        notifyQqSwitch = findViewById(R.id.notifyQqSwitch)
        notifyWxSwitch = findViewById(R.id.notifyWxSwitch)

        notifyWxSwitch?.setOnCheckedChangeListener { button, checked ->
            if(button.isPressed){
                MmkvUtils.setSaveObjParams("wx_switch",checked)
            }
        }

        notifyQqSwitch?.setOnCheckedChangeListener{button, checked ->
            if(button.isPressed){
                MmkvUtils.setSaveObjParams("qq_switch",checked)
            }
        }

        notifyDiscordSwitch?.setOnCheckedChangeListener { button, checked ->
            if(button.isPressed){
                MmkvUtils.setSaveObjParams("discord_switch",checked)
            }
        }
    }

    override fun initData() {
        val wxSwitch = MmkvUtils.getSaveParams("wx_switch",false)
        val qqSwitch = MmkvUtils.getSaveParams("qq_switch",false)
        val discordSwitch = MmkvUtils.getSaveParams("discord_switch",false)

        notifyWxSwitch?.isChecked = wxSwitch as Boolean
        notifyQqSwitch?.isChecked = qqSwitch as Boolean
        notifyDiscordSwitch?.isChecked = discordSwitch as Boolean

    }
}