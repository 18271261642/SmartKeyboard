package com.app.smartkeyboard;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.app.smartkeyboard.action.DebugLoggerTree;
import com.app.smartkeyboard.ble.ConnStatus;
import com.app.smartkeyboard.ble.ConnStatusService;
import com.app.smartkeyboard.utils.MmkvUtils;
import com.blala.blalable.BleApplication;
import com.blala.blalable.BleOperateManager;
import com.hjq.toast.ToastUtils;
import com.tencent.mmkv.MMKV;
import org.litepal.LitePal;
import timber.log.Timber;

/**
 * Created by Admin
 * Date 2023/1/4
 * @author Admin
 */
public class BaseApplication extends BleApplication {


    /**连接状态枚举**/
    private ConnStatus connStatus = ConnStatus.NOT_CONNECTED;
    private static BaseApplication baseApplication;

    private ConnStatusService connStatusService;

    @Override
    public void onCreate() {
        super.onCreate();

        initApp();


    }


    public static BaseApplication getBaseApplication(){
        return baseApplication;
    }

    public BleOperateManager getBleOperate(){
        return BleOperateManager.getInstance();
    }

    private void initApp(){
        baseApplication = this;
        //log
        Timber.plant(new DebugLoggerTree());
        //数据库
        LitePal.initialize(this);
        //Toast
        ToastUtils.init(this);
        //mmkv
        MMKV.initialize(this);
        MmkvUtils.initMkv();


        Intent intent = new Intent(this, ConnStatusService.class);
        this.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

    }


    public ConnStatusService getConnStatusService(){
        return connStatusService;
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            connStatusService =( (ConnStatusService.ConnBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            connStatusService = null;
        }
    };


    //获取连接状态
    public ConnStatus getConnStatus(){
        return connStatus;
    }

    //设置连接状态
    public void setConnStatus(ConnStatus connStatus){
        this.connStatus = connStatus;
    }
}
