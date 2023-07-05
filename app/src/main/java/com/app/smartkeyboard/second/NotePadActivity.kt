package com.app.smartkeyboard.second

import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.smartkeyboard.R
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.adapter.SecondNotePadAdapter
import com.app.smartkeyboard.bean.NoteBookBean
import com.app.smartkeyboard.viewmodel.NoteBookViewModel


/**
 * Created by Admin
 *Date 2023/7/4
 */
class NotePadActivity : AppActivity() {

    private val viewModel by viewModels<NoteBookViewModel>()

    private var secondNoteRecyclerView : RecyclerView ?= null
    private var adapter : SecondNotePadAdapter ?= null
    private var list : MutableList<NoteBookBean> ?= null



    override fun getLayoutId(): Int {
       return R.layout.activity_note_pad_layout
    }

    override fun initView() {
        secondNoteRecyclerView = findViewById(R.id.secondNoteRecyclerView)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        list = ArrayList<NoteBookBean>()


    }

    override fun initData() {
        viewModel.allNoteBookData.observe(this){

        }
    }

    override fun onResume() {
        super.onResume()
        getAllDbData()
    }

    //查询所有的数据
    private fun getAllDbData(){
        viewModel.getAllDbData()
    }
}