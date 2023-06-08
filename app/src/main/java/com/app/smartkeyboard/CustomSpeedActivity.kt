package com.app.smartkeyboard

import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.gif.GIFEncoder
import com.app.smartkeyboard.gif.GifMaker
import com.app.smartkeyboard.utils.ImageUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class CustomSpeedActivity : AppActivity() {


    private var previewGifImageView: ImageView? = null
    private var customSeekBar: SeekBar? = null

    private var seekBarValueTv: TextView? = null

    var gifPath: String? = null

    private var inputEdit : EditText ?= null


    override fun getLayoutId(): Int {
        return R.layout.activity_custom_speed_layout
    }

    override fun initView() {
        inputEdit = findViewById(R.id.inputEdit)
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

        findViewById<Button>(R.id.setEdit).setOnClickListener {

            val input = inputEdit?.text.toString()
            val number = input.toInt()

            //createGif(number * 30)

            reChangeGif(number * 30)
        }
    }


    private val handlers : Handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            Glide.with(this@CustomSpeedActivity).clear(previewGifImageView!!)
            val previewFile = gifPath + "/previews.gif"
            val uri = Uri.fromFile(File(previewFile))
            Glide.with(this@CustomSpeedActivity).asGif().load(uri).into(previewGifImageView!!)

        }
    }


    private fun createGif(delay : Int){
        val gifFile = File(gifPath+"/gif.gif")
        val filePath: String =
            gifPath+"/1.gif"

        val bitmapList = ImageUtils.getGifDataBitmapNoClip(gifFile)

        val encoder = GIFEncoder()
        encoder.init(bitmapList.get(0))
        encoder.start(filePath)
        encoder.setFrameRate(delay.toFloat())
        for (i in 1 until bitmapList.size) {
            encoder.addFrame(bitmapList.get(i))
        }
        encoder.finish()


        Glide.with(this@CustomSpeedActivity).clear(previewGifImageView!!)

        Glide.with(this@CustomSpeedActivity).asGif().load(gifFile).into(previewGifImageView!!)

    }


    //重新生成gif，根据间隔
    private fun reChangeGif( delay :Int) {
        val gifFile = File(gifPath+"/gif.gif")
        Timber.e("------gifFIle="+(gifFile?.exists())+" "+delay)
        if (gifFile.exists() == true) {
            val bitmapList = ImageUtils.getGifDataBitmapNoClip(gifFile)

            Timber.e("-------=bitmapList="+bitmapList?.size)
            val perviewFile = File(gifPath + "/previews.gif")
            if(perviewFile.exists()){
                val delete = perviewFile.delete()
                Timber.e("----delete="+delete)
            }


            val gifMaker = GifMaker(1)
            gifMaker.setOnGifListener { current, total ->
                Timber.tag("tags").e("---22---处理成gif了=" + current + " total=" + total)


            }
            GlobalScope.launch {
                val isSuccess = gifMaker.makeGif(bitmapList, gifPath + "/previews.gif",delay)
                Timber.e("-------isSuccess="+isSuccess+" "+delay)
                runOnUiThread {
                    if (isSuccess == true) {
                        handlers.sendEmptyMessageDelayed(0x00,500)

                    }
                }

            }

        }
    }

    override fun initData() {


        saveBitmap()


//        reChangeGif(150)

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