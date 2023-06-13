package com.app.smartkeyboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.ImageView
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.gif.GifMaker
import com.app.smartkeyboard.utils.ImageUtils
import com.app.smartkeyboard.utils.MmkvUtils
import com.bumptech.glide.Glide
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class CustomSpeedActivity : AppActivity() {


    private var previewGifImageView: ImageView? = null
    private var customSeekBar: SeekBar? = null

    private var seekBarValueTv: TextView? = null

    private var gifImageView : GifImageView ?= null


    private var cusSpeedSaveTv : ShapeTextView ?= null

    var gifPath: String? = null

    private val handlers : Handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 0x00){
//                val previewFile = File(gifPath + "/previews.gif")
//                Timber.e("-----路径="+previewFile.path)
//                Glide.with(this@CustomSpeedActivity).asGif().load(previewFile).into(previewGifImageView!!)

                val previewFile = File(gifPath + "/previews.gif")
                Timber.e("-----previewFile="+previewFile.path)
                val gifDrawable = GifDrawable(previewFile)
               // gifDrawable.stop()
                gifImageView?.setImageDrawable(gifDrawable)
//                gifDrawable.start()
                Timber.e("-------次数="+gifDrawable.loopCount)
            }

            if(msg.what == 0x01){
                val progress = msg.obj as Int
                saveBitmap(progress)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_custom_speed_layout
    }

    override fun initView() {
        cusSpeedSaveTv = findViewById(R.id.cusSpeedSaveTv)
        gifImageView = findViewById(R.id.gifImageView)
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


                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Timber.e("-----onStartTrackingTouch---="+seekBar?.progress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Timber.e("-----onStopTrackingTouch---="+seekBar?.progress)
                val progress = seekBar?.progress
                seekBarValueTv?.text = progress.toString()
                GlobalScope.launch {
                    if (progress != null) {
                      //  reChangeGif(progress * 30)

                      //  saveBitmap(progress)
                        val message = handlers.obtainMessage()
                        message.what = 0x01;
                        message.obj = progress
                        handlers.sendMessage(message)
                    }
                }
            }

        })
    }









    var gifMaker :GifMaker ?= null

    //重新生成gif，根据间隔
    private fun reChangeGif( delay :Int) {
      //  saveBitmap()

        //判断文件是否存在
        val gifFile = File(gifPath + "/gif.gif")
        Timber.e("------gifFIle="+(gifFile.exists())+" "+delay)
        if (gifFile.exists()) {


            val tempFile = File(gifPath + "/previews.gif")
//            if(tempFile.exists()){
//                tempFile.delete()
//            }

            val bitmapList = ImageUtils.getGifDataBitmapNoClip(gifFile)
            Timber.e("-------=bitmapList="+bitmapList.size)

            gifMaker = GifMaker(1)
            gifMaker?.setOnGifListener { current, total ->
                Log.e("tags", "---22---处理成gif了=" + current + " total=" + total)
               // handlers.sendEmptyMessageDelayed()
                if(current+1 == total){
                    GlobalScope.launch {
                       // Glide.get(this@CustomSpeedActivity).clearDiskCache()
                        handlers.sendEmptyMessageDelayed(0x00,300)
                    }
                }


//                runOnUiThread {
//                    if (current+1 == total) {
//                        val previewFile = File(gifPath + "/previews.gif")
//                        Timber.e("-----previewFile="+previewFile.path)
//                        val gifDrawable = GifDrawable(previewFile)
//                        gifImageView?.setImageDrawable(gifDrawable)
//
//                    }
//                }

            }
            GlobalScope.launch {
                gifMaker?.makeGif(bitmapList, gifPath + "/previews.gif",delay)

            }

        }
    }

    override fun initData() {

      val speed = MmkvUtils.getGifSpeed()
        customSeekBar?.max = 10
        customSeekBar?.progress = speed
        saveBitmap(speed)

       // reChangeGif(1 * 30)
    }


    private fun saveBitmap(speed : Int) {
        //val bitmap = BitmapFactory.decodeResource(resources,R.drawable.gif_speed)
        seekBarValueTv?.text = speed.toString()
        val drawable = GifDrawable(resources,R.drawable.gif_preview)
        drawable.setSpeed(speed.toFloat())
//        gifImageView?.maxWidth = 1000
//        gifImageView?.minimumWidth = 1000
        gifImageView?.setImageDrawable(drawable)

        MmkvUtils.saveGifSpeed(speed)


//
//        val outputStream = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        while (true) {
//            val bytesRead = inputStream.read(buffer)
//
//            if (bytesRead == -1)
//                break
//            outputStream.write(buffer, 0, bytesRead)
//
//        }
//        inputStream.close()
//        outputStream.close()

    }

}