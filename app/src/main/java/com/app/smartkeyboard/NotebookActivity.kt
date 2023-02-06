package com.app.smartkeyboard

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.adapter.NoteBookAdapter
import com.app.smartkeyboard.adapter.OnCommItemClickListener
import com.app.smartkeyboard.bean.NoteBookBean
import com.app.smartkeyboard.ble.ConnStatus
import com.app.smartkeyboard.viewmodel.NoteBookViewModel
import timber.log.Timber
import java.util.*

/**
 * 笔记页面
 * Created by Admin
 *Date 2023/1/10
 */
class NotebookActivity : AppActivity() {

    private val viewModel by viewModels<NoteBookViewModel>()

    private var noteBookRY : RecyclerView ?= null
    private var noteList : MutableList<NoteBookBean> ?= null
    private var noteAdapter : NoteBookAdapter ?= null

    //添加imgView
    private var noteBookAddImgView : ImageView ?= null
    //搜索
    private var noteBookSearchEdit : EditText ?= null
    //nodate
    private var noteBookEmptyTv : TextView ?= null


    var allList : MutableList<NoteBookBean> ?= null

    override fun getLayoutId(): Int {
       return R.layout.activity_notebook_layout
    }

    override fun initView() {
        noteBookEmptyTv = findViewById(R.id.noteBookEmptyTv)
        noteBookSearchEdit = findViewById(R.id.noteBookSearchEdit)
        noteBookAddImgView = findViewById(R.id.noteBookAddImgView)
        noteBookRY = findViewById(R.id.noteBookRY)
        val gridLayoutManager = GridLayoutManager(this,2)
        noteBookRY?.layoutManager = gridLayoutManager
        noteList = mutableListOf()
        noteAdapter = NoteBookAdapter(this, noteList!!)
        noteBookRY?.adapter = noteAdapter
        allList = mutableListOf()

        noteAdapter?.setOnCommClickListener(onItemClick)

        noteBookSearchEdit?.addTextChangedListener(textWatcher)
        noteBookAddImgView?.setOnClickListener {
            startActivity(EditNoteBookActivity::class.java)
//            BaseApplication.getBaseApplication().bleOperate.sendKeyBoardScreen()
        }
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            Timber.e("-------beforeTextChanged-----------")
            //s--未改变之前的内容
            //start--内容被改变的开始位置
            //count--原始文字被删除的个数
            //after--新添加的内容的个数

            //---------start和count结合从s中获取被删除的内容-------
            val deleText = s.toString().substring(start, start + count)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            Timber.e("-------onTextChanged-----------="+count+" "+(s))
            //s--改变之后的新内容
            //start--内容被改变的开始位置
            //before--原始文字被删除的个数
            //count--新添加的内容的个数

            //---------start和count结合从s中获取新添加的内容-------
            val addText = s.toString().substring(start, start + count)


        }

        override fun afterTextChanged(s: Editable) {
            Timber.e("-------afterTextChanged-----------="+s.toString()+" "+(TextUtils.isEmpty(s)))
            //s--最终内容
            if(TextUtils.isEmpty(s)){
               getAllDbData()
                return
            }
            val str = s.toString()

            val tempList = mutableListOf<NoteBookBean>()
            noteList?.forEach {
                if(it.noteTitle.toLowerCase(Locale.ROOT).contains(str.toLowerCase(Locale.ROOT))){
                    tempList.add(it)
                }
            }

            noteList?.clear()
            noteList?.addAll(tempList)
            noteAdapter?.notifyDataSetChanged()
        }
    }


    override fun initData() {

        viewModel.allNoteBookData.observe(this){
            if(it == null){
                showEmptyData()
                return@observe
            }
            noteBookRY?.visibility = View.VISIBLE
            noteBookEmptyTv?.visibility = View.GONE

            noteList?.clear()
            noteList?.addAll(it)
            noteList?.sortByDescending { it.noteTimeLong }
            noteAdapter?.notifyDataSetChanged()
        }
    }


    private fun showEmptyData(){
        noteList?.clear()
        noteAdapter?.notifyDataSetChanged()
        noteBookEmptyTv?.visibility = View.VISIBLE
        noteBookRY?.visibility = View.GONE

    }


    override fun onResume() {
        super.onResume()
        getAllDbData()
    }

    //查询所有的数据
    private fun getAllDbData(){
        viewModel.getAllDbData()
    }




    //item点击
    private val onItemClick : OnCommItemClickListener = OnCommItemClickListener {

        val noteBean = noteList?.get(it)
        if (noteBean != null) {
           // startActivity(EditNoteBookActivity::class.java, arrayOf("timeKey"), arrayOf(noteBean.saveTime))
            sendNotToDevice(noteBean.noteTitle,noteBean.noteTimeLong)
        }
     //   startActivity(EditNoteBookActivity::class.java)

    }


    //发送数据
    private fun sendNotToDevice(title : String,timeLong : Long){
        if(BaseApplication.getBaseApplication().connStatus != ConnStatus.CONNECTED){
            return
        }
        //时间戳
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeLong
        BaseApplication.getBaseApplication().bleOperate.sendKeyBoardNoteBook(title,calendar)
    }
}