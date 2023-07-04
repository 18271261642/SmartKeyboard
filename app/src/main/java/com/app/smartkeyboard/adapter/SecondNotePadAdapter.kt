package com.app.smartkeyboard.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.app.smartkeyboard.R
import com.app.smartkeyboard.bean.NoteBookBean
import com.app.smartkeyboard.utils.BikeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder

/**
 * Created by Admin
 *Date 2023/7/4
 */
 class SecondNotePadAdapter : BaseQuickAdapter<NoteBookBean,QuickViewHolder>() {


    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: NoteBookBean?) {
        holder.getView<TextView>(R.id.itemNoteBookTitleTv).text = item?.noteTitle
        holder.getView<TextView>(R.id.itemSecondTimeTv).text =
            item?.noteTimeLong?.let { BikeUtils.getFormatDate(it,"yyyy/MM/dd") }

        holder.getView<TextView>(R.id.itemSecondContentTv).text = item?.noteContent
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_second_note_pad_layout,parent)
    }
}