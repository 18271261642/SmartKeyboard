package com.blala.blalable.keyboard;

import android.util.Log;

import com.blala.blalable.Utils;

/**
 * Created by Admin
 * Date 2023/2/1
 * @author Admin
 */
public class KeyBoardConstant {


    private static byte[] sendData;

    public static byte[] getDialByte(DialCustomBean dialCustomBean){

        byte[] uiId = Utils.toByteArray((int) dialCustomBean.getUiFeature());
        byte[] binSize = Utils.toByteArray((int) dialCustomBean.getBinSize());
        byte[] unicode = Utils.stringToByte(Utils.getUnicode(dialCustomBean.getName()).replace("\\u", ""));//解码

//        byte[] send=new byte[]{
//                0x01,0x00,0x0B,
//                uiId[0],uiId[1],uiId[2],uiId[3],
//                binSize[0],binSize[1],binSize[2],binSize[3],
//                (byte) 0xff,(byte) 0xff,(byte) 0xff
//        };

        //gif指令
       byte[] array04 = new byte[]{0x04,0x00,0x04,0x00,0x00, (byte) 0xff, (byte) 0xfc};

       byte[] array05 = new byte[]{0x05,0x00,0x14, (byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};

        byte[] tnnpArr = Utils.hexStringToByte(keyValue(array04,array05));
        sendData=  Utils.getFullPackage(Utils.getPlayer("09", "03",tnnpArr));

        return sendData;
    }




    //获取起始位置
    public static byte[] getDialStartArray(){
        byte[] start = Utils.toByteArrayLength(16777215, 4);
        byte[] end = Utils.toByteArrayLength(16777215, 4);

        String key = keyValue(start, end);

        byte[] sendData = Utils.getFullPackage(Utils.getPlayer("08", "03", Utils.hexStringToByte(key)));

        Log.e("键盘","-------起始位置="+Utils.formatBtArrayToString(sendData));

        return sendData;
    }


    private static String keyValue(byte[] key, byte[] key1) {
        return Utils.getHexString(key) +
                Utils.getHexString(key1);

    }


    /**
     * 设置设备信息，连接成功后设置
     * @param isChinese 语言是否是中文
     * @return data
     */
    public static byte[] deviceInfoData(boolean isChinese){
        byte[] data = new byte[16];
        data[0] = 0x04;
        data[1] = 0x02;
        //性别 1男 2女
        data[2] = 0x02;
        //年龄
        data[3] = 0x12;
        //身高
        data[4] = (byte) 0xA0;
        //体重
        data[5] = 0x37;
        //系统语言 0中文 1英文
        data[6] = (byte) (isChinese ? 0x00 : 0x01);
        //时间
        data[7] = 0x00;
        //单位
        data[8] = 0x00;
        //系统 0ios 1安卓
        data[9] = 0x01;
        //左右手
        data[10] = 0x00;
        //温度
        data[11] = 0x00;
        //步数
        byte[] array = new byte[4];

        array[3] = (byte) (8000 & 0xFF);
        array[2] = (byte) ((8000 >> 8) & 0xFF);
        array[1] = (byte) ((8000 >> 16) & 0xFF);
        array[0] = (byte) ((8000 >> 24) & 0xFF);
        data[12] = array[0];
        data[13] = array[1];
        data[14] = array[2];
        data[15] = array[3];
        return data;
    }


    /**
     * 消息推送
     */
    public static byte[] getMsgNotifyData(int type,String title,String content){
        byte[] unicodeTitle = Utils.stringToByte(Utils.getUnicode(title).replace("\\u", ""));//解码
        byte[] unicodeContent = Utils.stringToByte(Utils.getUnicode(content).replace("\\u", ""));//解码

        byte[] unitCode  = Utils.hexStringToByte(keyValue((byte) type,unicodeTitle, unicodeContent));
        byte[] resultData =Utils.getFullPackage(unitCode);
        return resultData;
    }

    private static String keyValue(byte key, byte[] key1, byte[] key2) {
        return "0501010001" +//索引 和长度 长度现在都是1
                //  ByteUtil.getHexString(HexDump.toByteArray((short) key.length)) +
                Utils.getHexString(key) +
                "02" +
                Utils.getHexString(Utils.toByteArray((short) key1.length)) +
                Utils.getHexString(key1) +
                "03" +
                Utils.getHexString(Utils.toByteArray((short) key2.length)) +
                Utils.getHexString(key2)
                ;
    }
}
