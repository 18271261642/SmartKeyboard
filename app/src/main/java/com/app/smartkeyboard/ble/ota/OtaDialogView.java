package com.app.smartkeyboard.ble.ota;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;

import com.app.smartkeyboard.R;

public class OtaDialogView extends AppCompatDialog {

    private TextView lastVersionTv;
    private TextView currentVersionTv;


    public OtaDialogView(@NonNull Context context) {
        super(context);
    }

    public OtaDialogView(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected OtaDialogView(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ota_layout);
        initViews();


    }

    protected void initViews(){
        lastVersionTv = findViewById(R.id.lastVersionTv);
        currentVersionTv = findViewById(R.id.currentVersionTv);

    }


    //设置当前版本和最新版本
    public void setVersions(String current,String lastVersion){
        lastVersionTv.setText(lastVersion);
        currentVersionTv.setText(current);
    }
}
