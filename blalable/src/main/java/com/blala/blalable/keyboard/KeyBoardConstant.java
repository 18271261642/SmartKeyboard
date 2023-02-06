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


        byte[] send=new byte[]{
                0x01,0x00,0x0B,
                uiId[0],uiId[1],uiId[2],uiId[3],
                binSize[0],binSize[1],binSize[2],binSize[3],
                (byte) 0xff,(byte) 0xff,(byte) 0xff
        };

        sendData=  Utils.getFullPackage(Utils.getPlayer("09", "03",send));

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
}
