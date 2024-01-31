package com.serenegiant.usbcameratest9;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameracommon.CameraCallback;
import com.serenegiant.widget.XUSBCameraView;

import java.nio.ByteBuffer;

public class USBCameraDialog extends AppCompatDialog {
    private static final String TAG = "USBCameraDialog";

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
        assert xusbCameraView != null;
        xusbCameraView.setPreviewSize(960,720);
        findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
        xusbCameraView.setCameraCallback(new CameraCallback() {
            @Override
            public void onOpen() {
                Log.d(TAG, "onOpen() called");
            }

            @Override
            public void onClose() {
                Log.d(TAG, "onClose() called");
            }

            @Override
            public void onStartPreview() {
                Log.d(TAG, "onStartPreview() called");
            }

            @Override
            public void onStopPreview() {
                Log.d(TAG, "onStopPreview() called");
            }

            @Override
            public void onStartRecording() {
                Log.d(TAG, "onStartRecording() called");
            }

            @Override
            public void onStopRecording() {
                Log.d(TAG, "onStopRecording() called");
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "onError() called with: e = [" + e + "]");
            }

            @Override
            public void onFrame(ByteBuffer frame, int w, int h) {
                Log.d(TAG, "onFrame() called with: frame = [" + frame + "], w = [" + w + "], h = [" + h + "]");
            }

            @Override
            public void onCaptureFinish(String path) {
            }
        });

        xusbCameraView.postDelayed(() -> {
            xusbCameraView.connect(controlBlock, activity);
        }, 100);
    }

    public void open(USBMonitor.UsbControlBlock controlBlock, Activity activity) {
        this.activity = activity;
        this.controlBlock = controlBlock;
        if (!isShowing()) show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        xusbCameraView.release();
    }
}
