package com.app.smartkeyboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.gif.GifMaker
import com.app.smartkeyboard.utils.ImageUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class CustomSpeedActivity : AppActivity() {


    private var previewGifImageView: ImageView? = null
    private var customSeekBar: SeekBar? = null

    private var seekBarValueTv: TextView? = null

    var gifPath: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_custom_speed_layout
    }

    override fun initView() {
        seekBarValueTv = findViewById(R.id.seekBarValueTv)
        gifPath = getExternalFilesDir(null)?.path

        previewGifImageView = findViewById(R.id.previewGifImageView)
        customSeekBar = findViewById(R.id.customSeekBar)
        customSeekBar?.max = 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            customSeekBar?.min = 1
        }

        customSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Timber.e("-------prgr="+progress+" "+fromUser)
                if (fromUser) {
                    seekBarValueTv?.text = progress.toString()
                    GlobalScope.launch {
                        reChangeGif(progress * 30)
                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }
    var gifMaker :GifMaker ?= null

    //重新生成gif，根据间隔
    private fun reChangeGif( delay :Int) {
        //判断文件是否存在
        val gifFile = File(gifPath + "/gif.gif")
        Timber.e("------gifFIle="+(gifFile.exists())+" "+delay)
        if (gifFile.exists()) {

            val bitmapList = ImageUtils.getGifDataBitmapNoClip(gifFile)
            Timber.e("-------=bitmapList="+bitmapList.size)

            gifMaker = GifMaker(1)
            gifMaker?.setOnGifListener { current, total ->
                Log.e("tags", "---22---处理成gif了=" + current + " total=" + total)
                runOnUiThread {
                    if (total == bitmapList.size) {
                        Glide.with(this).clear(previewGifImageView!!)
                        val previewFile = gifPath + "/previews.gif"
                        Glide.with(this).asGif().load(previewFile).into(previewGifImageView!!)

                    }
                }

            }
            GlobalScope.launch {
                gifMaker?.makeGif(bitmapList, gifPath + "/previews.gif",delay)

            }

        }
    }

    override fun initData() {


        saveBitmap()

       // reChangeGif(30)

//        //判断文件是否存在
//        val gifFile = File(gifPath + "/gif.gif")
//        if (gifFile.exists()) {
//
//            val bitmapList = ImageUtils.getGifDataBitmapNoClip(gifFile)
//
//
//            val gifMaker = GifMaker(1)
//            gifMaker.setOnGifListener { current, total ->
//                Log.e("tags", "------处理成gif了=" + current + " total=" + total)
//                runOnUiThread {
//                    if (total == bitmapList.size) {
////                        Glide.with(this).clear(previewGifImageView!!)
////                        val previewFile = gifPath + "/preview.gif"
////                        Glide.with(this).asGif().load(previewFile).into(previewGifImageView!!)
//
//                    }
//                }
//
//
//            }
//            GlobalScope.launch {
//                gifMaker.makeGif(bitmapList, gifPath + "/preview.gif")
//
//            }
//
//        }
    }


    private fun saveBitmap() {
        //val bitmap = BitmapFactory.decodeResource(resources,R.drawable.gif_speed)


        val file = File(gifPath + "/gif.gif")
        val assetManager = assets

        val inputStream = assetManager.open("gif_preview.gif")

        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        while (true) {
            val bytesRead = inputStream.read(buffer)

            if (bytesRead == -1)
                break
            outputStream.write(buffer, 0, bytesRead)

        }
        inputStream.close()
        outputStream.close()

    }

}