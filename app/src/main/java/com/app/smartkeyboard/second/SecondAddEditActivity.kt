package com.app.smartkeyboard.second

import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.AppActivity

/**
 * 二代键盘编辑添加
 */
class SecondAddEditActivity :  AppActivity() {


    private var secondEditTitleEdit : EditText ?= null
    private var secondEditContentEdit : EditText ?= null
    private var secondEditSubmitTv : TextView ?= null
    private var secondAddBackTv : TextView ?= null
    private var secondAddBackImg : ImageView ?= null


    override fun getLayoutId(): Int {
        return R.layout.activity_second_add_edit_layout
    }

    override fun initView() {
        secondAddBackImg = findViewById(R.id.secondAddBackImg)
        secondEditTitleEdit = findViewById(R.id.secondEditTitleEdit)
        secondEditContentEdit = findViewById(R.id.secondEditContentEdit)
        secondEditSubmitTv = findViewById(R.id.secondEditSubmitTv)
        secondAddBackTv = findViewById(R.id.secondAddBackTv)
    }

    override fun initData() {

    }
}