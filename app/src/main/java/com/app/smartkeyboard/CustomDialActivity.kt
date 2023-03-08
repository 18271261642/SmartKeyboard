package com.app.smartkeyboard

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.app.smartkeyboard.action.AppActivity
import com.app.smartkeyboard.ble.ConnStatus
import com.app.smartkeyboard.img.CameraActivity
import com.app.smartkeyboard.img.CameraActivity.OnCameraListener
import com.app.smartkeyboard.img.ImageSelectActivity
import com.app.smartkeyboard.listeners.OnGetImgWidthListener
import com.app.smartkeyboard.utils.BitmapAndRgbByteUtil
import com.app.smartkeyboard.utils.GlideEngine
import com.app.smartkeyboard.utils.ImgUtil
import com.app.smartkeyboard.utils.ThreadUtils
import com.blala.blalable.Utils
import com.blala.blalable.keyboard.DialCustomBean
import com.blala.blalable.keyboard.KeyBoardConstant
import com.blala.blalable.listener.OnKeyBoardListener
import com.blala.blalable.listener.WriteBackDataListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.ToastUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import timber.log.Timber
import java.io.File

/**
 * 自定义表盘页面
 * Created by Admin
 *Date 2023/1/31
 */
class CustomDialActivity : AppActivity() {

    private val tags = "CustomDialActivity"

    //选择图片的按钮
    private var customSelectImgView: ImageView? = null

    //展示选择的图片
    private var customShowImgView: ImageView? = null

    //设置保存
    private var customSetDialTv: ShapeTextView? = null

    //相机
    private var cusDialCameraLayout : ShapeConstraintLayout ?= null
    //相册
    private var cusDialAlbumLayout : ShapeConstraintLayout ?= null


    //对象
    private var dialBean = DialCustomBean()

//    private var logTv : TextView ?= null

    //拍照的url
    private var imageUri : Uri ?= null

    private var lenght = 0


    private val stringBuilder = StringBuilder()



    //裁剪图片
    private var cropImgPath: String? = null
    private var resultCropUri: Uri? = null


    override fun getLayoutId(): Int {
        return R.layout.activity_custom_dial_layout
    }

    override fun initView() {
        cusDialAlbumLayout = findViewById(R.id.cusDialAlbumLayout)
        cusDialCameraLayout = findViewById(R.id.cusDialCameraLayout)
        customSelectImgView = findViewById(R.id.customSelectImgView)
        customShowImgView = findViewById(R.id.customShowImgView)
        customSetDialTv = findViewById(R.id.customSetDialTv)
      //  logTv = findViewById(R.id.logTv)

        setOnClickListener(customSelectImgView, customSetDialTv,cusDialAlbumLayout,cusDialCameraLayout)

        findViewById<TextView>(R.id.tmpTv1).setOnClickListener {

            val array = byteArrayOf(0x09,0x01,0x00)
            val resultArray = Utils.getFullPackage(array)
            BaseApplication.getBaseApplication().bleOperate.writeCommonByte(resultArray,object : WriteBackDataListener{
                override fun backWriteData(data: ByteArray?) {
                    Timber.e("-------result="+Utils.formatBtArrayToString(data))
                }

            })
        }

        customShowImgView?.setOnClickListener {

        }
    }

    override fun initData() {
        XXPermissions.with(this).permission(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ).request { permissions, all -> }




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            XXPermissions.with(this).permission(arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)).request{ permissions, all -> }
        }
         cropImgPath = Environment.getExternalStorageDirectory().path + "/Download"
//        cropImgPath = this.getExternalFilesDir(null)?.path

        Timber.e("-----path="+cropImgPath)
    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when (id) {
            //选择图片
            R.id.cusDialAlbumLayout -> {
                showSelectDialog()
            }

            //保存
            R.id.customSetDialTv -> {
                setDialToDevice()
            }

            //相机
            R.id.cusDialCameraLayout->{
                checkCamera()
            }
        }
    }


    //判断是否有相机权限
    private fun checkCamera(){
        if(XXPermissions.isGranted(this,Manifest.permission.CAMERA)){
            openCamera()

        }else{
            XXPermissions.with(this).permission(arrayOf( Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)).request(object : OnPermissionCallback{
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    if(all){
                        openCamera()
                    }
                }

            })
        }
    }

    //相机拍照
    private fun openCamera(){
        // 点击拍照

        // 点击拍照
        CameraActivity.start(this, object : OnCameraListener {
            override fun onSelected(file: File) {
                Timber.e("--------xxxx="+file.path)
                setSelectImg(file.path, 0)
//                // 当前选中图片的数量必须小于最大选中数
//                if (mSelectImage.size < mMaxSelect) {
//                    mSelectImage.add(file.path)
//                }
            }

            override fun onError(details: String) {
                toast(details)
            }
        })
    }




    var grbByte = byteArrayOf()

    private fun setDialToDevice() {
        if(BaseApplication.getBaseApplication().connStatus == ConnStatus.NOT_CONNECTED){
            ToastUtils.show(resources.getString(R.string.string_device_not_connect))
            return
        }

        showDialog(resources.getString(R.string.string_sync_ing))
        BaseApplication.getBaseApplication().connStatus = ConnStatus.IS_SYNC_DIAL
        //stringBuilder.delete(0,stringBuilder.length)
        showLogTv()

        var uiFeature = 65533

        ThreadUtils.submit {
            var bitmap = Glide.with(this)
                .asBitmap()
                .load(dialBean.imgUrl)
                .into(
                    Target.SIZE_ORIGINAL,
                    Target.SIZE_ORIGINAL
                ).get()
            grbByte = BitmapAndRgbByteUtil.bitmap2RGBData(bitmap)
            Timber.e("------大小=" + grbByte.size)
            //   ImgUtil.loadMeImgDialCircle(imgRecall, bitmap)
        }

        //生成新图并保存
//        val newBit = BitmapAndRgbByteUtil.loadBitmapFromView(customShowImgView)
//        var path = FileUtils.saveBitmapToSDCard(
//            this@CustomDialActivity,
//            newBit,
//            (System.currentTimeMillis() / 1000).toString()
//        )

        dialBean.uiFeature = uiFeature.toLong()
        dialBean.binSize = grbByte.size.toLong()
        dialBean.type = 1

        val resultArray = KeyBoardConstant.getDialByte(dialBean)
        val str = Utils.formatBtArrayToString(resultArray)
      //  stringBuilder.append("发送3.11.3指令:$str"+"\n")
        Timber.e("-------表盘指令=" +str )
        showLogTv()
        BaseApplication.getBaseApplication().bleOperate.startFirstDial(resultArray
        ) { data -> //880000000000030f0904 02
            /**
             * 0x01：传入非法值。例如 0x00000000
            0x02：等待 APP 端发送表盘 FLASH 数据
            0x03：设备已经有存储这个表盘，设备端调用并显示
            0x04：设备存储空间不够，需要 APP 端调用 3.11.5 处理
            0x05：其他高优先级数据在处理
             */
            /**
             * 0x01：传入非法值。例如 0x00000000
            0x02：等待 APP 端发送表盘 FLASH 数据
            0x03：设备已经有存储这个表盘，设备端调用并显示
            0x04：设备存储空间不够，需要 APP 端调用 3.11.5 处理
            0x05：其他高优先级数据在处理
             */

          //  stringBuilder.append("设备端返回指定非固化表盘概要信息状态指令: " + Utils.formatBtArrayToString(data)+"\n")
            showLogTv()

            if (data.size == 11 && data[8].toInt() == 9 && data[9].toInt() == 4 ) {

                val codeStatus = data[10].toInt()
                if(codeStatus == 1){
                    hideDialog()
                    ToastUtils.show("传入非法值!")
                    return@startFirstDial
                }
                //设备存储空间不够
                if(codeStatus == 4){
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED

                }

                if(codeStatus == 5){
                    hideDialog()
                    ToastUtils.show(resources.getString(R.string.string_device_busy))
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                    return@startFirstDial
                }

                val array = KeyBoardConstant.getDialStartArray()
               // stringBuilder.append("3.10.3 APP 端设擦写设备端指定的 FLASH 数据块" + Utils.formatBtArrayToString(array)+"\n")
                showLogTv()

                BaseApplication.getBaseApplication().bleOperate.setIndexDialFlash(array){
                    data->
                    Timber.e("-----大塔="+Utils.formatBtArrayToString(data))
                    //880000000000030f090402
                    if(data.size == 11 && data[0].toInt() == -120 && data[8].toInt() == 8 && data[9].toInt() == 4 && data[10].toInt() == 2 ){

                        /**
                         * 0x01：不支持擦写 FLASH 数据
                         * 0x02：已擦写相应的 FLASH 数据块
                         */

                        //880000000000030e 08 04 02
                        /**
                         * 0x01：不支持擦写 FLASH 数据
                         * 0x02：已擦写相应的 FLASH 数据块
                         */
                       // stringBuilder.append("3.10.4 设备端返回已擦写 FLASH 数据块的状态" + Utils.formatBtArrayToString(data)+"\n")

                       // stringBuilder.append("开始发送flash数据" +"\n")
                        showLogTv()
                        toStartWriteDialFlash()

                    }

                }
            }
        }
    }


    private fun toStartWriteDialFlash(){
        val start = Utils.toByteArrayLength(16777215, 4)
        val end = Utils.toByteArrayLength(16777215, 4)

        val startByte = byteArrayOf(
            0x00, 0xff.toByte(), 0xff.toByte(),
            0xff.toByte()
        )

        val resultArray = getDialContent(startByte, startByte, grbByte, 1000 + 701, -100, 0)
        Timber.e("-------reaulstArray="+resultArray.size+" "+resultArray[0].size)

        resultArray.forEach {

            Timber.e("-------内部的内容="+it.size)
            it.forEach {
                Timber.e("------里层="+it.size)
            }

        }
        BaseApplication.getBaseApplication().bleOperate.writeDialFlash(resultArray,object : OnKeyBoardListener{
            override fun onSyncFlash(statusCode: Int) {
                /**
                 * 0x01：更新失败
                 * 0x02：更新成功
                 * 0x03：第 1 个 4K 数据块异常（含 APP 端发擦写和实际写入的数据地址不一致），APP 需要重走流程
                 * 0x04：非第 1 个 4K 数据块异常，需要重新发送当前 4K 数据块
                 * 0x05：4K 数据块正常，发送下一个 4K 数据
                 * 0x06：异常退出（含超时，或若干次 4K 数据错误，设备端处理）
                 */

                if(statusCode == 1){
                    hideDialog()
                    ToastUtils.show(resources.getString(R.string.string_update_failed))
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                }
                if(statusCode == 2){
                    hideDialog()
                    ToastUtils.show(resources.getString(R.string.string_update_success))
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                }
                if(statusCode == 6){
                    hideDialog()
                    ToastUtils.show(resources.getString(R.string.string_error_exit))
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                }
            }

        })
    }


    private fun showLogTv(){
       // logTv?.text = stringBuilder.toString()
    }

    //选择图片，展示弹窗
    private fun showSelectDialog() {

        if(XXPermissions.isGranted(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))){
            choosePick()
            return
        }
        XXPermissions.with(this).permission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)).request(object : OnPermissionCallback{
            override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                if(all){
                    choosePick()
                }
            }
        })

    }


    //选择图片
    private fun choosePick() {

        ImageSelectActivity.start(this@CustomDialActivity
        ) { data -> setSelectImg(data.get(0), 0) }
    }

    private fun setSelectImg(localUrl: String, code: Int) {
        Timber.e("--------选择图片=$localUrl")
        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FileProvider.getUriForFile(this, "com.app.smartkeyboard.provider", File(localUrl))
        } else {
            Uri.fromFile(File(localUrl))
        }
        Timber.e("-----uri=$uri")
        startPhotoZoom(uri, code)
    }


    /**
     * 调用系统裁剪
     *
     */
    private fun startPhotoZoom(uri: Uri, code: Int) {
        cropImgPath = Environment.getExternalStorageDirectory().path + "/Download"
//        cropImgPath = this.getExternalFilesDir(null)?.path
        try {
            val intent = Intent("com.android.camera.action.CROP")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            val date = System.currentTimeMillis().toString()
            cropImgPath = "$cropImgPath/$date.jpg"
            Timber.e("--cropPath=$cropImgPath")
            val cutFile = File(cropImgPath)
            //                if (!cutFile.exists()) {
//                    FileUtil.createFile(cutFile.getAbsolutePath());
//                }
            var cRui = Uri.fromFile(cutFile)
            Timber.e("----000--cRui=$cRui")
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context == null) {
                    Toast.makeText(this, "getContext = null", Toast.LENGTH_SHORT).show()
                    return
                }
                cRui = FileProvider.getUriForFile(
                    context,
                    "com.app.smartkeyboard.provider",
                    cutFile
                )
                Timber.e("----11--cRui=$cRui")
                this.resultCropUri = cRui
            } else {
                cRui = Uri.fromFile(cutFile)
                this.resultCropUri = cRui
                Timber.e("----22--cRui=$cRui")
            }

            //所有版本这里都这样调用
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cutFile))

//               intent.putExtra(MediaStore.EXTRA_OUTPUT,  getUriForFile(NewShareActivity.this,cutFile));
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,  uri);
            //输入图片路径
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", 2)
            intent.putExtra("aspectY", 1)
            intent.putExtra("outputX", 320)
            intent.putExtra("outputY", 172)
            intent.putExtra("scale", false)
            intent.putExtra("scaleUpIfNeeded", true)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            intent.putExtra("return-data", false)
            startActivityForResult(intent, code)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if ((requestCode == 0x01 || requestCode == 0x00) && resultCode == RESULT_OK) {
                if (data == null) return
                // 得到图片的全路径
                val cropUri = data.data
                Timber.e("--------后的图片=" + (cropUri == null) + " " + (resultCropUri == null))

                Glide.with(this@CustomDialActivity).load(cropUri).into(customShowImgView!!)

                Timber.e("-------裁剪后的图片="+(File(cropImgPath)).path)
                val url = File(cropImgPath).path
                dialBean.imgUrl = url


                setDialToDevice()
            }


            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                // 图片选择结果回调
                val selectList = PictureSelector.obtainMultipleResult(data)
                val url = selectList[0].cutPath
                Timber.e("--------图片的url=" + url)
                ImgUtil.loadHead(customShowImgView!!, url, object : OnGetImgWidthListener {
                    override fun backImgWidthAndHeight(width: Int, height: Int) {
                        if (width < 320 && height < 172) {
                            return
                        }
                        dialBean?.imgUrl = url
                        GlideEngine.createGlideEngine()
                            .loadImage(this@CustomDialActivity, url, customShowImgView!!)
                    }

                })

            }
        }
    }


    private fun keyValue(
        startKey: ByteArray,
        endKey: ByteArray,
        sendData: ByteArray
    ): String {
        val length = Utils.getHexString(Utils.toByteArray(lenght))
        return "880000" + length + "000805010009" +  //索引,长度
                Utils.getHexString(startKey) +  //起始位
                Utils.getHexString(endKey) +  //结束位
                "0202FFFF" +  //含crc效验包,索引2,俩个字节的长度
                Utils.getHexString(sendData) //+
    }




    private fun getDialContent(
        startKey: ByteArray,
        endKey: ByteArray,
        count: ByteArray,
        type: Int,
        position: Int,
        dialId: Int
    ): MutableList<List<ByteArray>> {

        lenght = count.size+17
        var mList: MutableList<List<ByteArray>> = mutableListOf()
        var arraySize: Int = count.size / 4096

        //  var arraySize: Int = count.size / 4096
        val list: MutableList<ByteArray> = mutableListOf()
        if (count.size % 4096 > 0) {
            arraySize += 1
        }

        Timber.e("-------总的4096个包:"+arraySize)

        for (i in 0 until arraySize) {
            val srcStart = i * 4096
            var array = ByteArray(4096)
            if (count.size - srcStart < 4096) {
                array = ByteArray(count.size - srcStart)
                System.arraycopy(count, srcStart, array, 0, count.size - srcStart)
            } else
                System.arraycopy(count, srcStart, array, 0, array.size)
            list.add(array)
        }


        list.forEachIndexed { index, childrArry ->
            val ll: MutableList<ByteArray> = mutableListOf()
            var arraySize2: Int = childrArry.size / 243
            if (childrArry.size % 243 > 0) {
                arraySize2 += 1
            }

//            if (index == 0) {
//                TLog.error("arraySize2==" + arraySize2)
//                TLog.error("count.size==" + count.size)
//                TLog.error("childrArry.size==" + childrArry.size)
//            }

            for (i in 0 until arraySize2) {
                var array = ByteArray(243)
                if (i == 0 && index == 0) { //只有第一位的第一个需要
                    array = ByteArray(218)
                    System.arraycopy(childrArry, 0, array, 0, array.size)
//                    array = Utils.hexStringToByte(keyValue(startKey, endKey, array,array.size))
                    array = Utils.hexStringToByte(keyValue(startKey, endKey, array))
                    Timber.e("arrayi == 0 && index == 0==" + Utils.getHexString(array))
                } else if (i == (arraySize2 - 1)) {
                    var srcStart = i * 243
                    if (index == 0)
                        srcStart -= 25
//                    TLog.error("srcStart++"+srcStart)
//                    TLog.error("childrArry.size++"+childrArry.size)
                    val num = childrArry.size - (srcStart)
                    array = ByteArray(num)
//                    TLog.error("array.size++"+array.size)
                    System.arraycopy(childrArry, srcStart, array, 0, array.size)
                } else {
                    var srcStart = i * 243
                    if (index == 0) {
                        srcStart -= 25
//                        TLog.error("srcStart=="+srcStart)
//                        TLog.error("array=="+ByteUtil.getHexString(array))
//                        TLog.error("array=="+ array.size)
//                        TLog.error("srcStart==" +  srcStart)
                        //                       TLog.error("array==" +  array.size)
                    }

                    System.arraycopy(childrArry, srcStart, array, 0, array.size)
                    //  if(index==0)
//                    TLog.error("arrayi == ${i}=="+ByteUtil.getHexString(array))
                }
                val arrayXOR = Utils.byteMerger(array, Utils.byteXOR(array))
                if(i == 0 && index == 0){
                    Timber.e("------第一="+Utils.formatBtArrayToString(arrayXOR))
                }
                ll.add(arrayXOR)
            }
            mList.add(ll)
        }

        Timber.e("---------第一包="+Utils.formatBtArrayToString(mList.get(0).get(0)))

        return mList
    }


    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
    }
}