package com.blala.blalable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.ConnStatusListener;
import com.blala.blalable.listener.InterfaceManager;
import com.blala.blalable.listener.OnBleStatusBackListener;
import com.blala.blalable.listener.OnCommBackDataListener;
import com.blala.blalable.listener.OnExerciseDataListener;
import com.blala.blalable.listener.OnMeasureDataListener;
import com.blala.blalable.listener.OnRealTimeDataListener;
import com.blala.blalable.listener.OnSendWriteDataListener;
import com.blala.blalable.listener.WriteBack24HourDataListener;
import com.blala.blalable.listener.WriteBackDataListener;
import com.google.gson.Gson;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * Created by Admin
 * Date 2021/9/3
 * @author Admin
 */
public class BleManager {

    private static final String TAG = "BleManager";


    private static final String SAVE_BLE_MAC_KEY = "blala_ble_mac";

    private static BleManager bleManager;
    private static BluetoothClient bluetoothClient;

    private static Context mContext;

    //??????0?????????1?????????2??????
    private int dayTag ;

    private final BleConstant bleConstant = new BleConstant();


    private BleConnStatusListener bleConnStatusListener;


    private final InterfaceManager interfaceManager = new InterfaceManager();

    public void setBleConnStatusListener(BleConnStatusListener bleConnStatusListener) {
        this.bleConnStatusListener = bleConnStatusListener;
    }

    public void setOnBleBackListener (OnBleStatusBackListener onBleBackListener){
        this.interfaceManager.onBleBackListener = onBleBackListener;
    }


    public void setOnSendWriteListener(OnSendWriteDataListener onSendWriteListener){
        this.interfaceManager.onSendWriteDataListener = onSendWriteListener;
    }

    //??????????????????
    public void setOnRealTimeDataListener(OnRealTimeDataListener onRealTimeDataListener) {
        this.interfaceManager.onRealTimeDataListener = onRealTimeDataListener;
    }


    /**??????????????????**/
    public void setOnMeasureDataListener(OnMeasureDataListener onMeasureDataListener){
        this.interfaceManager.onMeasureDataListener = onMeasureDataListener;
    }

    /**????????????**/
    public void setOnExerciseDataListener(OnExerciseDataListener onExerciseDataListener){
        this.interfaceManager.onExerciseDataListener = onExerciseDataListener;
    }

    public static BleManager getInstance(Context context){
        mContext = context;
        bluetoothClient = new BluetoothClient(mContext);
        if(bleManager == null){
            synchronized (BleManager.class){
                if(bleManager == null){
                    bleManager = new BleManager();
                }
            }
        }
        return bleManager;
    }


    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0x00){
                sendCommBroadcast(-1);
            }
        }
    };




    /**
     * ????????????
     * @param searchResponse ??????
     * @param duration ????????????
     * @param times ????????????
     *   eg:duration=10 * 1000;times=1 ?????????10s?????????????????????10s
     */
    public void startScanBleDevice(final SearchResponse searchResponse, int duration, int times){

        final SearchRequest searchRequest = new SearchRequest.Builder()
                .searchBluetoothLeDevice(duration,times)
                .build();
        bluetoothClient.search(searchRequest, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                searchResponse.onSearchStarted();
            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                searchResponse.onDeviceFounded(searchResult);
            }

            @Override
            public void onSearchStopped() {
                searchResponse.onSearchStopped();
            }

            @Override
            public void onSearchCanceled() {
                searchResponse.onSearchCanceled();
            }
        });

    }


    /**
     * ????????????
     * @param searchResponse ??????
     * @param duration ????????????
     * @param times ????????????
     *   eg:duration=10 * 1000;times=1 ?????????10s?????????????????????10s
     */
    public void startScanBleDevice(final SearchResponse searchResponse, boolean scanClass,int duration, int times){

        if(!scanClass){
            startScanBleDevice(searchResponse,duration,times);
            return;
        }
        final SearchRequest searchRequest = new SearchRequest.Builder()
                .searchBluetoothLeDevice(duration,times)
                .searchBluetoothClassicDevice(10 * 1000)
                .build();
        bluetoothClient.search(searchRequest, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                searchResponse.onSearchStarted();
            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                searchResponse.onDeviceFounded(searchResult);
            }

            @Override
            public void onSearchStopped() {
                searchResponse.onSearchStopped();
            }

            @Override
            public void onSearchCanceled() {
                searchResponse.onSearchCanceled();
            }
        });

    }



    /**
     * ????????????
     */
    public void stopScan(){
        if(bluetoothClient != null){
            bluetoothClient.stopSearch();
        }
    }

    //??????Mac????????????????????????
    public void connBleDeviceByMac(String mac, String bleName, ConnStatusListener connStatusListener){
        connBleDevice(mac,bleName,connStatusListener);
    }

    //????????????
    public void disConnDevice(){
        String spMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(spMac))
            return;
        bluetoothClient.stopSearch();
        bluetoothClient.disconnect(spMac);
        bluetoothClient.unregisterConnectStatusListener(spMac,connectStatusListener);
        BleSpUtils.remove(mContext,SAVE_BLE_MAC_KEY);

    }

    public void disConnDeviceNotRemoveMac(){
        String spMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(spMac))
            return;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            BluetoothDevice bleDevice = bluetoothAdapter.getRemoteDevice(spMac);
            if (bleDevice != null) {
                unpairDevice(bleDevice);
            }
        }
        bluetoothClient.stopSearch();
        bluetoothClient.disconnect(spMac);
        bluetoothClient.unregisterConnectStatusListener(spMac,connectStatusListener);
        BleSpUtils.remove(mContext,SAVE_BLE_MAC_KEY);
    }
    //???????????????BluetoothDevice.removeBond?????????????????????
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * // Constants.REQUEST_READ??????????????????
     * // Constants.REQUEST_WRITE??????????????????
     * // Constants.REQUEST_NOTIFY??????????????????????????????
     * // Constants.REQUEST_RSSI?????????????????????????????????
     */
    public void clearRequest(){
        String mac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(mac))
            return;
        bluetoothClient.clearRequest(mac,Constants.REQUEST_WRITE);
        bluetoothClient.clearRequest(mac,Constants.REQUEST_NOTIFY);

    }


    private synchronized void connBleDevice(final String bleMac, final String bleName, final ConnStatusListener connectResponse){
        BleSpUtils.put(mContext,SAVE_BLE_MAC_KEY,bleMac);

        int status = bluetoothClient.getConnectStatus(bleMac);

        Log.e(TAG,"************?????????="+bleMac+"--????????????="+status);

        bluetoothClient.registerConnectStatusListener(bleMac,connectStatusListener);
        BleConnectOptions options = (new com.inuker.bluetooth.library.connect.options.BleConnectOptions.Builder()).setConnectRetry(2).setConnectTimeout(30000).setServiceDiscoverRetry(1).setServiceDiscoverTimeout(20000).build();
        bluetoothClient.connect(bleMac, options, new BleConnectResponse() {
            @Override
            public void onResponse(final int code, final BleGattProfile data) {
                if(data == null || data.getServices() == null)
                    return;
                List<BleGattService> serviceList = data.getServices();
                Log.e(TAG,"-----onResponse="+code+"\n"+new Gson().toJson(serviceList));

                if(code == 0){  //????????????????????????????????????
                    //???????????????OTA??????????????????OTA?????????????????????
                    (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                        public void run() {

                            //?????????????????????????????????

                            setNotifyData(bleMac,bleConstant.SERVICE_UUID,bleConstant.READ_UUID,connectResponse);

                        }
                    }, 2000L);
                    connectResponse.connStatus(code);
                }
            }
        });

    }


    public synchronized void clearListener(){
        if(interfaceManager.writeBack24HourDataListener != null)
            interfaceManager.writeBack24HourDataListener = null;
    }

    /**????????????????????????????????????????????????????????????**/
    private synchronized void setSaveNotifyData(String mac,UUID serviceUUid,UUID notifyUid){
        bluetoothClient.notify(mac, serviceUUid, notifyUid, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                Log.e(TAG,"-----??????????????????="+uuid1.toString()+" "+Utils.formatBtArrayToString(bytes));
                if(interfaceManager.writeBack24HourDataListener != null){
                    interfaceManager.writeBack24HourDataListener.onWriteBack(bytes);
                }

                if(interfaceManager.onExerciseDataListener != null){
                    interfaceManager.onExerciseDataListener.backExerciseData(bytes);
                }

            }

            @Override
            public void onResponse(int i) {

            }
        });
    }



    public void setClearListener(){
        if(interfaceManager.writeBackDataListener != null){
            interfaceManager.writeBackDataListener = null;
        }
        if(interfaceManager.writeBack24HourDataListener != null)
            interfaceManager.writeBack24HourDataListener = null;

    }

    public void setClearExercise(){
        if(interfaceManager.onExerciseDataListener != null){
            interfaceManager.onExerciseDataListener = null;
        }
    }



    //?????????????????????????????????app????????????????????????????????????
    private synchronized void setNotifyData(String mac,UUID serviceUUid,UUID notifyUUid,ConnStatusListener connStatusListener){
        bluetoothClient.notify(mac, serviceUUid, notifyUUid, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                Log.e(TAG,"------??????????????????="+uuid1.toString()+" "+Utils.formatBtArrayToString(bytes));
                if(interfaceManager.writeBackDataListener != null){
                    interfaceManager.writeBackDataListener.backWriteData(bytes);
                }

                //??????????????????????????? 011701
                if(bytes.length == 3 && bytes[0] == 1 && bytes[1] == 23 && bytes[2] == 1){
                    sendCommBroadcast(BleConstant.BLE_COMPLETE_EXERCISE_ACTION,0);
                }

                //0151ff ??????????????????????????? ??????

                //023cffff ????????????????????????



                if(bytes.length == 3 && bytes[0] ==1 && (bytes[1]& 0xff) == 0x15 && bytes[2] == 1 ){    //??????
                    sendCommBroadcast(0x01);
                }

                if(bytes.length == 3 && bytes[0] == 1 && (bytes[1]& 0xff) == 81){   //??????????????????
                    if(interfaceManager.onMeasureDataListener != null)
                        interfaceManager.onMeasureDataListener.onMeasureHeart(bytes[2] & 0xff,System.currentTimeMillis());
                    handler.sendEmptyMessageDelayed(0x00,1500);
                }

                if(bytes.length == 4 && (bytes[0] & 0xff) == 2 && (bytes[1] & 0xff) == 60){ //??????????????????
                    if(interfaceManager.onMeasureDataListener != null)
                        interfaceManager.onMeasureDataListener.onMeasureBp(bytes[2] & 0xff ,bytes[3] & 0xff,System.currentTimeMillis());
                    handler.sendEmptyMessageDelayed(0x00,1500);
                }

                // 013e60 ??????
                if(bytes.length == 3 && bytes[0] == 1 && (bytes[1]& 0xff) == 62){
                    if(interfaceManager.onMeasureDataListener != null)
                        interfaceManager.onMeasureDataListener.onMeasureSpo2(bytes[2] & 0xff,System.currentTimeMillis());
                    handler.sendEmptyMessageDelayed(0x00,1500);
                }
            }

            @Override
            public void onResponse(int i) {
                connStatusListener.setNoticeStatus(i);
            }
        });
    }

    /**W561B?????????????????????**/
    private synchronized void w561BNotifyRealData(String bleMac,UUID serverUUid,UUID uuid,ConnStatusListener connStatusListener){
        bluetoothClient.notify(bleMac, serverUUid, uuid, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                Log.e(TAG,"------w61b????????????="+Utils.formatBtArrayToString(bytes));
                if(bytes.length == 2 ){
                    //????????????
                    int hr = bytes[1] & 0xff;
                    if(interfaceManager.onRealTimeDataListener != null){
                        interfaceManager.onRealTimeDataListener.realTimeData(hr,0,0,0);
                    }

                }
            }

            @Override
            public void onResponse(int i) {
                connStatusListener.setNoticeStatus(i);
            }
        });
    }


    /**??????????????????**/
    private synchronized void notifyRealtime(String bleMac,UUID serUUId,UUID realUUid){
        bluetoothClient.notify(bleMac, serUUId, realUUid, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID uuid, UUID uuid1, byte[] bytes) {
                if(bytes[0] == 2){ //?????????????????????
                    //????????????
                    int realHr = bytes[1] & 0xff;
                    //??????
                    int realStep = Utils.getIntFromBytes((byte) 0x00,bytes[6],bytes[5],bytes[4]);
                    //?????????
                    int realKcal = Utils.getIntFromBytes((byte) 0x00,bytes[9],bytes[8],bytes[7]);
                    //??????
                    int realDis = Utils.getIntFromBytes((byte) 0x00,bytes[12],bytes[11],bytes[10]);

//                    Log.e(TAG,"-----????????????="+realHr+" "+realStep+" "+realKcal+" "+realDis);

                    if(interfaceManager.onRealTimeDataListener != null)
                        interfaceManager.onRealTimeDataListener.realTimeData(realHr,realStep,realKcal,realDis);
                }
            }

            @Override
            public void onResponse(int i) {

            }
        });
    }



    /**????????????**/
    public synchronized void readDeviceBatteryValue(OnCommBackDataListener onCommBackDataListener){
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        bluetoothClient.read(bleMac, bleConstant.BATTERY_SERVER_UUID, bleConstant.BATTERY_READ_UUID, new BleReadResponse() {
            @Override
            public void onResponse(int i, byte[] bytes) {
                if(bytes == null)
                    return;
                Log.e(TAG,"-------????????????="+Utils.formatBtArrayToString(bytes));
                int batteryValue = bytes[0] & 0xff;
                if(onCommBackDataListener != null)
                    onCommBackDataListener.onIntDataBack(new int[]{batteryValue});
            }
        });
    }


    //????????????
    private synchronized void setNotiData(String bleMac, UUID serUUID, UUID notiUUID, final ConnStatusListener connStatusListener){
        bluetoothClient.notify(bleMac, serUUID, notiUUID, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, final byte[] value) {
                 Log.e(TAG,"---111-----????????????="+ Arrays.toString(value));
                if(value[0] == 2 && value[1] == -1 && value[2] == 64 && value[3] == dayTag){

                }
                if(interfaceManager.writeBackDataListener != null){
                    interfaceManager.writeBackDataListener.backWriteData(value);
                }

                if(value[0] == 1 && value[1] == 19){    //??????????????????
                    if(interfaceManager.onBleBackListener != null)
                        interfaceManager.onBleBackListener.intoMusicStatus(value[2] & 0xff);
                }

                if(value[0] == 1 && value[1] == 20){    //????????????
                    if(interfaceManager.onBleBackListener != null)
                        interfaceManager.onBleBackListener.findPhone();
                }

                //????????????
                if(value[0] == 1 && value[1] == 82 && value[2] == 2){

                }


                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 1000L);

            }

            @Override
            public void onResponse(int code) {
                connStatusListener.setNoticeStatus(code);
            }
        });
    }

    //??????????????????
    public synchronized void writeDataToDevice(byte[] data, WriteBackDataListener writeBackDataListener){
        Log.e(TAG,"-----????????????="+Utils.formatBtArrayToString(data));
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        interfaceManager.setWriteBackDataListener(writeBackDataListener);
        bluetoothClient.write(bleMac,bleConstant.SERVICE_UUID,bleConstant.WRITE_UUID,data,bleWriteResponse);
    }
    //??????????????????
    public synchronized void writeDataToDevice(byte[] data){
        Log.e(TAG,"-----????????????="+Arrays.toString(data));
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        bluetoothClient.write(bleMac,bleConstant.SERVICE_UUID,bleConstant.WRITE_UUID,data,bleWriteResponse);
    }




    /**??????????????????,24???????????????**/
    public synchronized void write24HourDataToDevice(byte[] data, WriteBack24HourDataListener writeBackDataListener){
        Log.e(TAG,"-----????????????="+Arrays.toString(data));
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        interfaceManager.setWriteBack24HourDataListener(writeBackDataListener);
        bluetoothClient.write(bleMac,bleConstant.SERVICE_UUID,bleConstant.WRITE_UUID,data,bleWriteResponse);
    }


    /**
     * ????????????????????????
     */
    public synchronized void writeKeyBoardDialData(byte[] data, WriteBackDataListener writeBackDataListener){
      //  this.dayTag = day;
        Log.e(TAG,"-----??????????????????????????????="+data.length +"  "+Utils.formatBtArrayToString(data));
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        interfaceManager.setWriteBackDataListener(writeBackDataListener);
        bluetoothClient.write(bleMac, bleConstant.SERVICE_UUID,bleConstant.KEYBOARD_DIAL_WRITE_UUID,data,bleWriteResponse);
    }



    //??????????????????
    public synchronized void writeExcDataToDevice(byte[] data, WriteBackDataListener writeBackDataListener){
        Log.e(TAG,"-----????????????="+Arrays.toString(data));
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        interfaceManager.setWriteBackDataListener(writeBackDataListener);
        bluetoothClient.write(bleMac, bleConstant.SERVICE_UUID,bleConstant.SAVE_DATA_SEND_UUID,data,bleWriteResponse);
    }

    public synchronized void writeWatchFaceData(byte[] data,WriteBackDataListener writeBackDataListener){
        Log.e(TAG,"-----??????????????????="+Arrays.toString(data));
        String bleMac = (String) BleSpUtils.get(mContext,SAVE_BLE_MAC_KEY,"");
        if(TextUtils.isEmpty(bleMac))
            return;
        interfaceManager.setWriteBackDataListener(writeBackDataListener);
        bluetoothClient.write(bleMac, bleConstant.SERVICE_UUID,bleConstant.WATCH_FACE_UUID,data,bleWriteResponse);
    }



    //?????????????????????????????????
    private final BleConnectStatusListener connectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {

            Log.e(TAG,"---mmmmm-????????????manager="+mac+" "+status);
            if(mac != null && status == Constants.STATUS_DISCONNECTED){
                sendCommBroadcast(BleConstant.BLE_SOURCE_DIS_CONNECTION_ACTION);
            }
            if(bleConnStatusListener != null){
                bleConnStatusListener.onConnectStatusChanged(mac==null?"mac":mac,status);
            }
        }
    };

    private final BleWriteResponse bleWriteResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };


    private void sendCommBroadcast(int...value){
        Intent intent = new Intent();
        intent.setAction(BleConstant.COMM_BROADCAST_ACTION);
        intent.putExtra(BleConstant.COMM_BROADCAST_KEY,value);
        mContext.sendBroadcast(intent);
    }

    private void sendCommBroadcast(String action,int...value){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(BleConstant.COMM_BROADCAST_KEY,value);
        mContext.sendBroadcast(intent);
    }
}
