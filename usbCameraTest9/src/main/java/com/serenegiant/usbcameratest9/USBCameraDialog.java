package com.serenegiant.usbcameratest9;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.widget.XUSBCameraView;

public class USBCameraDialog extends AppCompatDialog {

    public USBCameraDialog(@NonNull Context context) {
        super(context);
    }

    private XUSBCameraView xusbCameraView;
    USBMonitor.UsbControlBlock controlBlock;
    Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_usb_camera);
        xusbCameraView = findViewById(R.id.xcv);
    }

    @Override
    public void onStart() {
        super.onStart();
        xusbCameraView.postDelayed(() -> {
            xusbCameraView.setOwnerActivity(activity);
            xusbCameraView.connect(controlBlock);
        }, 900);
    }

    public void open(USBMonitor.UsbControlBlock controlBlock, Activity activity) {
        this.activity=activity;
        this.controlBlock=controlBlock;
        if (!isShowing()) show();

    }

    @Override
    public void dismiss() {
        super.dismiss();
        xusbCameraView.close();
    }
}
