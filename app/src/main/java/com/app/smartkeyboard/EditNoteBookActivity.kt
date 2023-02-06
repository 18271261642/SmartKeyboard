package com.app.smartkeyboard

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.bean.DbManager
import com.app.smartkeyboard.bean.NoteBookBean
import com.app.smartkeyboard.utils.BikeUtils
import com.hjq.toast.ToastUtils

/**
 * Created by Admin
 *Date 2023/1/10
 */
class EditNoteBookActivity : AppActivity() {

    //标题
    private var editNoteBookTitleTv : TextView ?= null
    //输入的标题
    private var editNoteBookTitleEdit : EditText ?= null
    //字数
    private var editNoteBookNumberTv : TextView ?= null
    //输入的内容
    private var editNoteBookEditText : AppCompatEditText ?= null
    //返回
    private var editNoteBookBackImgView : ImageView ?= null
    //保存
    private var editNoteBookSaveImgView : ImageView ?= null
    //时间
    private var editNoteBookTimeTv : TextView ?= null

    override fun getLayoutId(): Int {
        return R.layout.activity_edit_notebook_layout
    }

    override fun initView() {
        editNoteBookTitleTv = findViewById(R.id.editNoteBookTitleTv)
        editNoteBookNumberTv = findViewById(R.id.editNoteBookNumberTv)
        editNoteBookEditText = findViewById(R.id.editNoteBookEditText)
        editNoteBookBackImgView = findViewById(R.id.editNoteBookBackImgView)
        editNoteBookSaveImgView = findViewById(R.id.editNoteBookSaveImgView)
        editNoteBookTitleEdit = findViewById(R.id.editNoteBookTitleEdit)
        editNoteBookTimeTv = findViewById(R.id.editNoteBookTimeTv)

        setOnClickListener(editNoteBookBackImgView,editNoteBookSaveImgView)
    }

    override fun initData() {
        editNoteBookTitleTv?.text = "添加"
        editNoteBookEditText?.addTextChangedListener(textWatcher)

        editNoteBookTimeTv?.text = BikeUtils.formatKeyboardTime(System.currentTimeMillis(),this)

        //时间戳
        val timeStr = intent.getStringExtra("timeKey")
        if (timeStr != null) {
            queryNoteBookData(timeStr)
        }

    }



    //查询对应的数据，根据时间戳查询 yyyy-MM-dd HH:mm:ss格式
    private fun queryNoteBookData(timeStr : String){
        val dataBean = DbManager.getInstance().queryNoteBookByTime(timeStr)
        if(dataBean != null){
            editNoteBookTitleTv?.text = "编辑"
            editNoteBookTitleEdit?.setText(dataBean.noteTitle.toString())
            editNoteBookTimeTv?.text = BikeUtils.formatKeyboardTime(dataBean.noteTimeLong,this)
            editNoteBookEditText?.setText(dataBean.noteContent)
        }

    }


    private val textWatcher : TextWatcher = object : TextWatcher{


        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            val length = p0?.length
            editNoteBookNumberTv?.text = length.toString()+" 字"
        }

    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when(id){
            //返回
            R.id.editNoteBookBackImgView->{
                finish()
            }
            //保存
            R.id.editNoteBookSaveImgView->{
                saveOrUpdateData()
            }
        }
    }



    //保存或修改数据
    private fun saveOrUpdateData(){
        //标题
        val inputTitle = editNoteBookTitleEdit?.text.toString()
        //内容
        val inputContent = editNoteBookEditText?.text.toString()

        if(TextUtils.isEmpty(inputTitle)){
            ToastUtils.show("请输入标题！")
            return
        }

        val noteBookBean = NoteBookBean()
        noteBookBean.noteTitle = inputTitle
        noteBookBean.noteContent = inputContent
        noteBookBean.noteTimeLong = System.currentTimeMillis()
        noteBookBean.saveTime = BikeUtils.getCurrDate()
        noteBookBean.saveTime = BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")

        val  isSave = DbManager.getInstance().saveOrUpdateData(noteBookBean)
        if(isSave){
            //ToastUtils.show("保存成功!")
            finish()
        }
    }
}