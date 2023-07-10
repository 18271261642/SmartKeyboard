package com.app.smartkeyboard.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.app.smartkeyboard.R

class AddAlarmDialog : AppCompatDialog {

    constructor(context: Context) : super (context){

    }


    constructor(context: Context, theme : Int) : super (context, theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_alarm_layout)


    }
}