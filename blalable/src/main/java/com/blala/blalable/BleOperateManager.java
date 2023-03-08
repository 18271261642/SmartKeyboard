package com.blala.blalable;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toolbar;

import com.blala.blalable.bean.WeatherBean;
import com.blala.blalable.blebean.AlarmBean;
import com.blala.blalable.blebean.CommBleSetBean;
import com.blala.blalable.blebean.CommTimeBean;
import com.blala.blalable.keyboard.KeyBoardConstant;
import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.ConnStatusListener;
import com.blala.blalable.listener.OnBleStatusBackListener;
import com.blala.blalable.listener.OnCommBackDataListener;
import com.blala.blalable.listener.OnCommTimeSetListener;
import com.blala.blalable.listener.OnExerciseDataListener;
import com.blala.blalable.listener.OnKeyBoardListener;
import com.blala.blalable.listener.OnMeasureDataListener;
import com.blala.blalable.listener.OnRealTimeDataListener;
import com.blala.blalable.listener.OnSendWriteDataListener;
import com.blala.blalable.listener.OnWatchFaceVerifyListener;
import com.blala.blalable.listener.OnWriteProgressListener;
import com.blala.blalable.listener.WriteBack24HourDataListener;
import com.blala.blalable.listener.WriteBackDataListener;
import com.google.gson.Gson;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import androidx.annotation.NonNull;


/**
 * Created by Admin
 * Date 2022/8/8
 */
public class BleOperateManager {

    private static final String TAG = "BleOperateManager";

    private static BleOperateManager bleOperateManager;

    private final BleManager bleManager = BleApplication.getInstance().getBleManager();

    private final BleConstant bleConstant = new BleConstant();

    public static BleOperateManager getInstance() {
        if (bleOperateManager == null) {
            synchronized (BleOperateManager.class) {
                if (bleOperateManager == null)
                    bleOperateManager = new BleOperateManager();
            }
        }
        return bleOperateManager;
    }

    public BleOperateManager() {
    }

    private OnKeyBoardListener keyBoardListener;


    private List<byte[]> detailDialList = new ArrayList<>();
    private int detailDialCount = 0;


    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x00) {
                Log.e(TAG, "-----发送表盘index=" + dialCount + "  " + dialList.size());
                if (dialCount < dialList.size()) {
                    detailDialList.clear();
                    List<byte[]> indexData = dialList.get(dialCount);
                    detailDialList.addAll(indexData);
                    detailDialCount = 0;
                    dialCount++;
                    handler.sendEmptyMessageDelayed(0x01, 20);
                    // sendWriteKeyBoardData(indexData);
                } else { //发送完了
                    Log.e(TAG, "---------全部发送万了");
                }

            }

            //每个4K包的分包发送
            if (msg.what == 0x01) {
                Log.e(TAG, "------4K包详细发送=" + detailDialCount + " " + detailDialList.size());
                if (detailDialCount < detailDialList.size()) {
                    byte[] detailData = detailDialList.get(detailDialCount);
                    detailDialCount++;
                    sendWriteKeyBoardData(detailData);
                } else {
                    //一个4K包里面的内容发送完了
                    Log.e(TAG, "---------一个4K包发送全部发送万了");
                    handler.sendEmptyMessageDelayed(0x00, 50);
                }
            }
        }
    };


    //设置写入指令监听
    public void setOnOperateSendListener(OnSendWriteDataListener onOperateSendListener) {
        bleManager.setOnSendWriteListener(onOperateSendListener);
    }


    //搜索
    public void scanBleDevice(SearchResponse searchResponse, int duration, int times) {
        bleManager.startScanBleDevice(searchResponse, duration, times);
    }

    //搜索
    public void scanBleDevice(SearchResponse searchResponse, boolean isScanClass, int duration, int times) {
        bleManager.startScanBleDevice(searchResponse, isScanClass, duration, times);
    }

    //停止搜索
    public void stopScanDevice() {
        bleManager.stopScan();
    }

    //设置连接状态监听，在连接之前设置
    public void setBleConnStatusListener(BleConnStatusListener bleConnStatusListener) {
        bleManager.setBleConnStatusListener(bleConnStatusListener);
    }


    //设置实时的数据
    public void setRealTimeDataListener(OnRealTimeDataListener onRealTimeDataListener) {
        bleManager.setOnRealTimeDataListener(onRealTimeDataListener);
    }

    //监听测量数据返回
    public void setMeasureDataListner(OnMeasureDataListener onMeasureDataListener) {
        bleManager.setOnMeasureDataListener(onMeasureDataListener);
    }


    public void setClearListener() {
        bleManager.setClearListener();
    }

    public void setClearExercisListener() {
        bleManager.setClearExercise();
    }

    //设置手表监听，用于监听音乐状态和查找手机
    public void setBleBackStatus(OnBleStatusBackListener onBleStatusBackListener) {
        bleManager.setOnBleBackListener(onBleStatusBackListener);
    }

    //连接
    public void connYakDevice(String bleName, String bleMac, ConnStatusListener connStatusListener) {
        bleManager.connBleDeviceByMac(bleMac, bleName, connStatusListener);
    }

    //断连连接
    public void disConnYakDevice() {
        bleManager.disConnDevice();
    }

    public void disConnNotRemoveMac() {
        bleManager.disConnDeviceNotRemoveMac();
    }

    //写通用的设置，直接写数据
    public void writeCommonByte(byte[] bytes, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bytes, writeBackDataListener);
    }


    //进入工厂测试
    public void setIntoTestModel(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.intoTestModel(), writeBackDataListener);
    }


    //写通用的设置，直接写数据
    public void writeCommonByte(ArrayList<byte[]> listBytes, WriteBackDataListener writeBackDataListener) {
        for (byte[] bt : listBytes)
            bleManager.writeDataToDevice(bt, writeBackDataListener);
    }


    //查找手表
    public void findDevice(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.findDevice(), writeBackDataListener);
    }

    //查找手表
    public void findDevice() {
        bleManager.writeDataToDevice(bleConstant.findDevice(), writeBackDataListener);
    }

    //读取电量
    public void readBattery(OnCommBackDataListener onCommBackDataListener) {
        bleManager.readDeviceBatteryValue(onCommBackDataListener);
    }

    //使手表关机或进入复位 1关机，2清除数据
    public void setDevicePowerOrRecycler(int value, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.powerOff(value), writeBackDataListener);
    }

    //进入拍照
    public void setIntoTakePhotoStatus(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setIntoPhoto(), writeBackDataListener);
    }

    //获取版本信息
    public void getDeviceVersionData(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getDeviceVersion(), writeBackDataListener);
    }

    //获取版本信息
    public void getDeviceVersionData(OnCommBackDataListener onCommBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getDeviceVersion(), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if ((data[0] & 0xff) == 15 && (data[1] & 0xff) == 255 && data[2] == 1) {

                    //有多少天24小时的数据
                    int validDay = data[5] & 0xff;


                    //硬件版本
                    int hardVersion = data[8] & 0xff;
                    //软件版本
                    byte[] sortArray = new byte[8];
                    System.arraycopy(data, 9, sortArray, 0, sortArray.length);
                    try {
                        String versionStr = new String(sortArray, "UTF-8");

                        onCommBackDataListener.onStrDataBack(hardVersion + "", versionStr, String.valueOf(validDay));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    //获取电量
    public void getDeviceBattery(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getDeviceBattery(), writeBackDataListener);
    }

    //获取时间
    public void getDeviceTime(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getCurrTime(), writeBackDataListener);
    }

    //设置时间
    public void syncDeviceTime(WriteBackDataListener writeBackDataListener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        Log.e(TAG, "---日期=" + year + "\n" + month + "\n" + day + "\n" + hour + "\n" + minute + "\n" + seconds);
        bleManager.writeDataToDevice(bleConstant.syncTime(year, month, day, hour, minute, seconds), writeBackDataListener);
    }


    //设置用户信息
    public void setUserInfoData(int year, int month, int day, int weight, int height, int sex, int maxHeart, int minHeart, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.syncUserInfo(year, month, day, weight, height, sex, maxHeart, minHeart), writeBackDataListener);
    }

    //获取用户信息
    public void getUserInfoData(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getUserInfo(), writeBackDataListener);
    }

    //读取久坐提醒
    public void getLongSitData(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getLongSitData(), writeBackDataListener);
    }


    //设置目标步数
    public void setStepTarget(int step, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.stepGoal(step), writeBackDataListener);
    }

    //读取计步目标
    public void readStepTarget(OnCommBackDataListener onCommBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.readStepGoal(), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) { //04 ff 0c 00 32 c8
                if (data[0] == 0x04 && (data[1] & 0xff) == 0xff && (data[2] & 0xff) == 0x0C) {
                    int step = Utils.getIntFromBytes((byte) 0x00, data[5], data[4], data[3]);
                    Log.e(TAG, "-----目标步数=" + step);
                    if (onCommBackDataListener != null)
                        onCommBackDataListener.onIntDataBack(new int[]{step});
                }
            }
        });
    }


    //读取久坐提醒
    public void getLongSitData(OnCommTimeSetListener onCommTimeSetListener) {
        bleManager.writeDataToDevice(bleConstant.getLongSitData(), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if (data.length < 4)
                    return;
                if (data[0] == 6 && (data[1] & 0xff) == 0xff & data[2] == 3) {
                    //间隔
                    int level = data[3] & 0xff;
                    int startHour = data[4] & 0xff;
                    int startMinute = data[5] & 0xff;
                    int endHour = data[6] & 0xff;
                    int endMinute = data[7] & 0xff;

                    CommTimeBean commTimeBean = new CommTimeBean(0, startHour, startMinute, endHour, endMinute);
                    commTimeBean.setLevel(level);

                    boolean isOpen = level != 0;
                    commTimeBean.setSwitchStatus(isOpen ? 1 : 0);
                    if (onCommTimeSetListener != null)
                        onCommTimeSetListener.onCommTimeData(commTimeBean);
                    bleManager.setClearListener();
                }
            }
        });
    }


    //设置久坐提醒
    public void setLongSitData(int startHour, int startMinute, int interval, int endHour, int endMinute, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setLongSitData(startHour, startMinute, interval, endHour, endMinute), writeBackDataListener);
    }

    //设置久坐提醒
    public void setLongSitData(CommTimeBean commTimeBean, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setLongSitData(commTimeBean.getStartHour(), commTimeBean.getStartMinute(), commTimeBean.getLevel(), commTimeBean.getEndHour(), commTimeBean.getEndMinute()), writeBackDataListener);
    }


    //读取抬腕亮屏
    public void getWristData(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getWristData(), writeBackDataListener);
    }

    //读取抬腕亮屏
    public void getWristData(OnCommTimeSetListener onCommTimeSetListener) {
        bleManager.writeDataToDevice(bleConstant.getWristData(), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if (data.length < 4)
                    return;
                if (data[0] == 6 && (data[1] & 0xff) == 0xff & data[2] == 8) {
                    //开关
                    int isOpen = data[3] & 0xff;
                    int startHour = data[4] & 0xff;
                    int startMinute = data[5] & 0xff;
                    int endHour = data[6] & 0xff;
                    int endMinute = data[7] & 0xff;

                    CommTimeBean commTimeBean = new CommTimeBean(isOpen, startHour, startMinute, endHour, endMinute);
                    if (onCommTimeSetListener != null)
                        onCommTimeSetListener.onCommTimeData(commTimeBean);
                    bleManager.setClearListener();
                }
            }
        });
    }


    //设置抬腕亮屏
    public void setWristData(boolean isOpen, int startHour, int startMinute, int endHour, int endMinute, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setWristData(isOpen, startHour, startMinute, endHour, endMinute), writeBackDataListener);
    }

    //设置抬腕亮屏
    public void setWristData(CommTimeBean commTimeBean, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setWristData(commTimeBean.getSwitchStatus() == 1, commTimeBean.getStartHour(), commTimeBean.getStartMinute(), commTimeBean.getEndHour(), commTimeBean.getEndMinute()), writeBackDataListener);
    }


    //获取心率开光状态
    public void getHeartStatus(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getHeartStatus(), writeBackDataListener);
    }

    //设置心率开关状态
    public void setHeartStatus(boolean isOpen, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setHeartStatus(isOpen), writeBackDataListener);
    }

    //读取勿扰模式
    public void getDNTStatus(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getDNTStatus(), writeBackDataListener);
    }

    //读取勿扰模式
    public void getDNTStatus(OnCommTimeSetListener onCommTimeSetListener) {
        bleManager.writeDataToDevice(bleConstant.getDNTStatus(), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if (data.length < 4)
                    return;
                if (data[0] == 6 && (data[1] & 0xff) == 255 && data[2] == 6) {
                    //开关
                    int isOpen = data[3] & 0xff;
                    int startHour = data[4] & 0xff;
                    int startMinute = data[5] & 0xff;
                    int endHour = data[6] & 0xff;
                    int endMinute = data[7] & 0xff;

                    CommTimeBean commTimeBean = new CommTimeBean(isOpen, startHour, startMinute, endHour, endMinute);
                    if (onCommTimeSetListener != null)
                        onCommTimeSetListener.onCommTimeData(commTimeBean);
                    bleManager.setClearListener();
                }
            }
        });
    }

    //设置勿扰模式
    public void setDNTStatus(boolean isOpen, int startHour, int startMinute, int endHour, int endMinute, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setDNTStatus(isOpen, startHour, startMinute, endHour, endMinute), writeBackDataListener);
    }

    //设置勿扰模式
    public void setDNTStatus(CommTimeBean commTimeBean, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setDNTStatus(commTimeBean.getSwitchStatus() == 1, commTimeBean.getStartHour(), commTimeBean.getStartMinute(), commTimeBean.getEndHour(), commTimeBean.getEndMinute()), writeBackDataListener);
    }


    //进入拍照
    public void intoTakePhoto(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.intoPhoto(), writeBackDataListener);
    }

    //测量血氧状态
    public void measureSo2Status(boolean isOpen, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.measureSpo2(isOpen), writeBackDataListener);
    }

    //测量心率
    public void measureHeartStatus(boolean isOpen, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.measureHeartStatus(isOpen), writeBackDataListener);
    }

    //测量血压
    public void measureBloodStatus(boolean isOpen, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.measureBloodStatus(isOpen), writeBackDataListener);
    }

    //获取通用设置
    public void getCommonSetting(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getCommonSetData(), writeBackDataListener);
    }


    //设置通用
    public void setCommonSetting(boolean isKm, boolean is24Hour, boolean is24HourHeart, boolean isTmp, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setCommonData(isKm, is24Hour, is24HourHeart, isTmp), writeBackDataListener);
    }

    private final StringBuilder setBuilder = new StringBuilder();

    //写通用的设置，直接写数据
    public void setCommonSetting(CommBleSetBean commBleSetBean, WriteBackDataListener writeBackDataListener) {
        setBuilder.delete(0, setBuilder.length());
        byte[] byArray = new byte[12];
        byArray[0] = 0x0a;
        byArray[1] = 0x32;
        //设置完后翻转
        //0公英制
        setBuilder.append(commBleSetBean.getMetric());
        setBuilder.append(commBleSetBean.getLanguage());
        setBuilder.append(commBleSetBean.getTimeType());
        setBuilder.append(0);
        setBuilder.append(commBleSetBean.getIs24Heart());
        setBuilder.append(commBleSetBean.getTemperature());
        setBuilder.append("00");
        String setStr = setBuilder.reverse().toString();
        Log.e(TAG, "--通用设置=" + setStr);
        byte bt = Utils.bitToByte(setStr);
        byArray[3] = bt;
        byArray[4] = 3;
        // Utils.bitToByte()
        bleManager.writeDataToDevice(byArray, writeBackDataListener);
    }


    //获取内置表盘
    public void getLocalDial(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getLocalDial(), writeBackDataListener);
    }

    //设置内置表盘
    public void setLocalDial(int index, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setLocalDial(index), writeBackDataListener);
    }

    //获取背光等级
    public void getBackLight(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getBackLight(), writeBackDataListener);
    }

    //设置背光等级和时长
    public void setBackLight(int light, int interval, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setBackLight(light, interval), writeBackDataListener);
    }


    //读取某天数据
    public void getDayForData(int day, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getDaySport(day), writeBackDataListener);
    }

    //读取某天数据
    public void getDay24HourForData(int day, WriteBack24HourDataListener writeBack24HourDataListener) {
        bleManager.write24HourDataToDevice(bleConstant.getDaySport(day), writeBack24HourDataListener);
    }

    /**
     * 将back清空
     **/
    public void setDay24HourClear() {
        bleManager.clearListener();
    }


    public void getCountDayData(int day, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getCountData(day), writeBackDataListener);
    }


    /**
     * 提取锻炼数据
     **/
    public void getExerciseData(int num, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getExerciseByte(num), writeBackDataListener);
    }


    /**
     * 监听锻炼数据回调
     **/
    public void setExerciseDataListener(OnExerciseDataListener onExerciseDataListener) {
        bleManager.setOnExerciseDataListener(onExerciseDataListener);
    }

    //清除锻炼数据
    public void clearAllDeviceExerciseData(WriteBackDataListener writeBackDataListener) {
        bleManager.writeExcDataToDevice(bleConstant.clearExerciseByte(), writeBackDataListener);
    }

    //设置闹钟
    public void setAlarmId(AlarmBean alarmBean, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.setAlarm(alarmBean.getAlarmIndex(), alarmBean.isOpen(), alarmBean.getRepeat(), alarmBean.getHour(), alarmBean.getMinute()), writeBackDataListener);
    }

    //读取闹钟
    public void readAlarm(int alarmId, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.readAlarm(alarmId), writeBackDataListener);
    }

    private List<AlarmBean> list = new ArrayList<>();

    public void readAllAlarm() {
        list.clear();
        int alarmIndex = 0;
        readAlarm(alarmIndex);
    }

    private void readAlarm(int id) {
        readAlarm(id, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if ((data[0] & 0xff) == 18 && (data[1] & 0xff) == 255) {
                    if (data[3] == 0x00) {
                        AlarmBean alarmBean = analysisAlarm(data);
                        list.add(alarmBean);
                        readAlarm(0x01);
                    }

                    if (data[3] == 0x01) {
                        AlarmBean alarmBean = analysisAlarm(data);
                        list.add(alarmBean);
                        readAlarm(0x02);
                    }
                    if (data[3] == 0x02) {
                        AlarmBean alarmBean = analysisAlarm(data);
                        list.add(alarmBean);
                    }
                }
            }
        });
    }


    public AlarmBean analysisAlarm(byte[] array) {
        AlarmBean alarmBean = new AlarmBean();
        alarmBean.setAlarmIndex(array[3] & 0xff);
        alarmBean.setOpen(array[4] == 0x01);
        alarmBean.setRepeat(array[5]);
        alarmBean.setHour(array[6] & 0xff);
        alarmBean.setMinute(array[7] & 0xff);
        byte[] msgArray = new byte[12];
        System.arraycopy(array, 8, msgArray, 0, msgArray.length);
        alarmBean.setMsg(new String(msgArray));
        return alarmBean;

    }


    //读取当前表盘
    public void readCurrentDial(OnCommBackDataListener onCommBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getLocalDial(), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if ((data[0] & 0xff) == 2 && (data[1] & 0xff) == 0xff) {
                    //表盘
                    int id = data[3] & 0xff;
                    onCommBackDataListener.onIntDataBack(new int[]{id});
                }
            }
        });
    }

    //设置本地表盘
    public void setLocalDial(int id) {
        bleManager.writeDataToDevice(bleConstant.setLocalDial(id), writeBackDataListener);
    }


    private ArrayList<byte[]> appList = new ArrayList<>();

    private final Handler msgHandler = new Handler(Looper.myLooper()) {
        @NonNull
        @Override
        public String getMessageName(@NonNull Message message) {
            return super.getMessageName(message);


        }
    };


    /**
     * 发送app通知
     *
     * @param type                  类型
     * @param title                 标题
     * @param content               内容
     * @param writeBackDataListener
     */
    public static String formatTwoStr(int number) {
        String strNumber = String.format("%02d", number);
        return strNumber;
    }


    //发送歌曲
    public void setMusicStatus(String musicName, String musicContTime, String musicCurrentTime, WriteBackDataListener writeBackDataListener) {
        ArrayList<byte[]> musicLt = bleConstant.sendMusic(musicName, musicContTime, musicCurrentTime);

        for (int i = 0; i < musicLt.size(); i++) {
            byte[] bt = musicLt.get(i);
            bleManager.writeDataToDevice(bt, writeBackDataListener);
        }
    }


    //发送天气
    public void sendWeatherData(String cityName, WriteBackDataListener writeBackDataListener) {

        byte[] secondByte = new byte[20];
        secondByte[0] = 0x04;
        secondByte[1] = 12;
        secondByte[2] = (byte) 2;
        secondByte[3] = 0x20;
        secondByte[4] = (byte) (20 >> 8);
        secondByte[5] = 0x21;
        secondByte[6] = 0x30;
        secondByte[7] = 0x10;
        secondByte[8] = 0x25;

        byte[] wb = new byte[]{0x04, 0x12, 0x02, 0x13, 0x00, 0x18, 0x17, 0x12, 0x01};

        bleManager.writeDataToDevice(wb, writeBackDataListener);
//        ArrayList<byte[]> weatherLt = bleConstant.weatherListByte(cityName);
//        for(int i = 0;i<weatherLt.size();i++){
//            byte[] wtByte = weatherLt.get(i);
//            bleManager.writeDataToDevice(wtByte,writeBackDataListener);
//        }
    }


    /**
     * 发送天气
     *
     * @param weatherList 天气集合
     */
    public void sendWeatherData(List<WeatherBean> weatherList, WriteBackDataListener writeBackDataListener) {
        int index = 1;
        for (WeatherBean wb : weatherList) {
            index = index + 1;
            bleManager.writeDataToDevice(bleConstant.weatherList(index, wb.getAirQuality(), wb.getTemperature(), wb.getMaxTemper(), wb.getMinTemper(), wb.getWeather()), writeBackDataListener);
        }

    }

    public void sendIndexBack(int index, OnWatchFaceVerifyListener onWatchFaceVerifyListener) {
        bleManager.writeWatchFaceData(bleConstant.sendCurrWatchFace(index), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if (data.length == 4) {
                    if (data[0] == 2 && ((data[1] & 0xff) == 255) && (data[2] & 0xff) == 97)
                        if (onWatchFaceVerifyListener != null)
                            onWatchFaceVerifyListener.isVerify(true, index);
                }
            }
        });
    }

    public void sendWatchFaceIndex(int index, byte[] data, OnWatchFaceVerifyListener onWatchFaceVerifyListener) {
        bleManager.writeWatchFaceData(data, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                if (data.length == 4) {
                    if (data[0] == 2 && ((data[1] & 0xff) == 255) && (data[2] & 0xff) == 97) {
                        if (onWatchFaceVerifyListener != null)
                            onWatchFaceVerifyListener.isVerify(true, index);
                    }
                }

            }
        });
    }




    public void sendSelectDial(List<RawFileBean> list, OnWriteProgressListener onWriteProgressListener) {


        sendIndexBack(1, new OnWatchFaceVerifyListener() {
            @Override
            public void isVerify(boolean isSuccess, int position) {
                Log.e(TAG, "----发送第一个表盘=" + isSuccess);
                if (isSuccess) {
                    sendDialContent(list, 1);
                }
            }
        });
    }

    int tmpIndex = 0;
    private List<byte[]> currListByte = new ArrayList<>();

    private void sendDialContent(List<RawFileBean> list, int index) {
        tmpIndex = -1;
        currListByte.clear();
        byte[] faceByteArray = AppUtils.getArrayByFaceInd(list, index - 1);

        currListByte = Utils.dialByteList(faceByteArray);
        Log.e(TAG, "-----所有表盘长度=" + currListByte.size() + "  " + tmpIndex);
        handler.sendEmptyMessage(0x00);
    }


    private void writeData(byte[] b) {
        bleManager.writeWatchFaceData(b, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG, "----写入表盘返回=" + Arrays.toString(data));
                handler.sendEmptyMessageDelayed(0x00, 100);
            }
        });
    }


    int positionIndex = 0;
    ArrayList<byte[]> listBytes = new ArrayList<>();

    private void sendIndexFace(int facePackIndex, byte[] arrays, OnWatchFaceVerifyListener onWatchFaceVerifyListener) {
        listBytes.clear();
        listBytes = Utils.dialByteList(arrays);

        Log.e(TAG, "--22--表盘文件有多少包=" + listBytes.size());

        //循环发送包
        do {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendWatchFaceIndex(positionIndex, listBytes.get(positionIndex), new OnWatchFaceVerifyListener() {
                        @Override
                        public void isVerify(boolean isSuccess, int position) {
                            Log.e(TAG, "----循环发送包=" + isSuccess + " position=" + position);
                            if (isSuccess) {
                                Message message = handler.obtainMessage();
                                message.what = 0x00;
                                Bundle bundle = new Bundle();
                                bundle.putInt("index", positionIndex++);

                            }
                            positionIndex++;
                            Log.e(TAG, "---循环发送=" + isSuccess + " " + position);
                        }
                    });
                }
            }, 200);

        } while (positionIndex != listBytes.size() - 1);

        facePackIndex++;
        Log.e(TAG, "--一个表盘发送完成=" + facePackIndex);
        if (onWatchFaceVerifyListener != null)
            onWatchFaceVerifyListener.isVerify(true, facePackIndex);
    }


    /**
     * 同步键盘的时间
     */
    public void syncKeyBoardTime(){

        byte[] timeByte = bleConstant.syncTime();
        byte[] resultData = Utils.getFullPackage(timeByte);
        bleManager.writeDataToDevice(resultData,writeBackDataListener);

    }
    /**
     * 同步键盘的时间
     */
    public void syncKeyBoardTime(WriteBackDataListener writeBackDataListener){

        byte[] timeByte = bleConstant.syncTime();
        byte[] resultData = Utils.getFullPackage(timeByte);
        bleManager.writeDataToDevice(resultData,writeBackDataListener);

    }



    /**
     * 设置设备基本信息，连接成功后就设置
     */
    public void setFirstDeviceInfo(boolean isChinese,WriteBackDataListener writeBackDataListener){
        byte[] data = KeyBoardConstant.deviceInfoData(isChinese);

        byte[] resultData = Utils.getFullPackage(data);

        bleManager.writeDataToDevice(resultData,writeBackDataListener);
    }


    /**
     * 消息推送，目前只推送微信
     */
    public void sendNotifyMsgData(int key,String title,String content){
        bleManager.writeDataToDevice(KeyBoardConstant.getMsgNotifyData(key,title,content));
    }


    //连接成功第一步获取设备的状态
    public void getKeyBoardStatus() {
        byte[] btArray = new byte[]{0x00, 0x13, 0x00};
        byte[] statusArray = Utils.getFullPackage(btArray);

        bleManager.writeDataToDevice(statusArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                //88 00 00 00 00 00 06 14 00 14 01 00 01 00
                if (data.length == 14 && data[6] == 6 && data[7] == 20 && data[10] == 1) {
                    sendKeyBoardScreen();
                }
            }
        });
    }


    //亮屏
    public void sendKeyBoardScreen() {
        byte[] data = new byte[]{0x01, 0x1C, 0x01, 0x00, 0x01, 0x01};
        byte[] logoArray = Utils.getFullPackage(data);

        bleManager.writeDataToDevice(logoArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {

            }
        });
    }

    /**
     * 将string类型的byte数组转成bytep[]
     *
     * @param str
     * @return
     */
    public static byte[] stringToByte(String str) {
        byte[] data = new byte[str.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = Integer.valueOf(str.substring(0 + i * 2, 2 + i * 2), 16).byteValue();
        }
        return data;
    }


    //设置非固化表盘
    public void setLocalKeyBoardDial() {
        String str = "880000000000060009070000FFFE";
        byte[] array = Utils.hexStringToByte(str);
        bleManager.writeDataToDevice(array, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {

            }
        });

    }

    //发送记事本
    public void sendKeyBoardNoteBook(String title, Calendar noteCalendar) {
        String unitCode = Utils.getUnicode(title).replace("\\u", "");

        //  00 68 00 68 00 68 00 6a 00 6a 00 68

        byte[] titleArray = stringToByte(unitCode);


        Log.e(TAG, "-------标题=" + title + "\n" + unitCode + "\n" + Utils.formatBtArrayToString(titleArray));

        int year = noteCalendar.get(Calendar.YEAR);
        int month = noteCalendar.get(Calendar.MONTH) + 1;
        int day = noteCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = noteCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = noteCalendar.get(Calendar.MINUTE);
        int second = noteCalendar.get(Calendar.SECOND);

        //星期
        int week = noteCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        Log.e(TAG, "---------week=" + week);

        byte[] timeArray = new byte[11];
        timeArray[0] = 0x01;
        timeArray[2] = 0x08;
        timeArray[3] = Utils.intToSecondByteArray(year)[1];
        timeArray[4] = Utils.intToSecondByteArray(year)[0];
        timeArray[5] = (byte) (month & 0xff);
        timeArray[6] = (byte) (day & 0xff);
        timeArray[7] = (byte) (hour & 0xff);
        timeArray[8] = (byte) (minute & 0xff);
        timeArray[9] = (byte) (second & 0xff);
        timeArray[10] = (byte) week;
        //时间
        String timeStr = Utils.getHexString(timeArray);

        Log.e(TAG, "------时间=" + timeStr);

        //内容

        int contentLength = titleArray.length;
        int l1 = Utils.intToSecondByteArray(contentLength)[1];
        int l2 = Utils.intToSecondByteArray(contentLength)[0];

        String conStr = "02" + String.format("%02x", l1) + String.format("%02x", l2) + Utils.getHexString(titleArray);

        String resultStr = "040A" + timeStr + conStr;

        byte[] tempContentArray = Utils.hexStringToByte(resultStr);

        //88 00 00 00 00 00 1c d1
        Log.e(TAG, "-------内如=" + conStr + "\n" + resultStr);

        String t = "8800000000001600 040A 0100 08  07E7 0106 0304 0504 02 0006  8B B0 4E 8B 67 2C";
        //8800000000001ccf0 040a 0100 08 07e7 01 0a 12 13 37 05 02 0120068  00680068006a006a0068
        //88 00 00 00 00 00 1c cf 00 40 a0 10 00 80 7e 70 10 a1 21 33 70 50 20 12 00 68 00 68 00 68 00 6a 00 6a 00 68
        //88 00 00 00 00 00 1c cf0 040a 0100 08 e707 01 0a 12 13 37 05 02 0120 06800680068006a006a0068
        //8800000000001a2f0010008e707010a1213370502012006800680068006a006a0068
        byte[] noteArray = Utils.getFullPackage(tempContentArray);


        Log.e(TAG, "---------记事本=" + Utils.formatBtArrayToString(noteArray));

        bleManager.writeDataToDevice(noteArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {

            }
        });
    }


    //第一步，开始写入表盘
    public void startFirstDial(byte[] data, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(data, writeBackDataListener);
    }


    //APP 端设擦写设备端指定的 FLASH 数据块
    public void setIndexDialFlash(byte[] data, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(data, writeBackDataListener);
    }


    //总的大小
    private int dialCount = 0;
    //表盘数据
    private List<List<byte[]>> dialList = new ArrayList<>();


    //开始写入表盘flash数据
    public void writeDialFlash(List<List<byte[]>> sourceList, OnKeyBoardListener onKeyBoardListener) {
        dialList.clear();
        dialList.addAll(sourceList);
        dialCount = 0;
        this.keyBoardListener = onKeyBoardListener;
        handler.sendEmptyMessageDelayed(0x00, 200);
    }


    private void sendWriteKeyBoardData(byte[] data) {
        bleManager.writeKeyBoardDialData(data, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG, "---------4K包里面的内容返回=" + Utils.formatBtArrayToString(data));
//                handler.sendEmptyMessageDelayed(0x01,100);
                //4K包里面的内容返回 880000000000030c080602
                /**
                 * 0x01：更新失败
                 * 0x02：更新成功
                 * 0x03：第 1 个 4K 数据块异常（含 APP 端发擦写和实际写入的数据地址不一致），APP 需要重走流程
                 * 0x04：非第 1 个 4K 数据块异常，需要重新发送当前 4K 数据块
                 * 0x05：4K 数据块正常，发送下一个 4K 数据
                 * 0x06：异常退出（含超时，或若干次 4K 数据错误，设备端处理）
                 */
                if (data.length == 11 && data[0] == -120 && data[8] == 8 && data[9] == 6) {
                    int code = data[10] & 0xff;
                    if (keyBoardListener != null) {
                        keyBoardListener.onSyncFlash(code);
                    }
                }

            }
        });
        handler.sendEmptyMessageDelayed(0x01, 20);
    }


    private final WriteBackDataListener writeBackDataListener = new WriteBackDataListener() {
        @Override
        public void backWriteData(byte[] data) {

        }
    };


}
