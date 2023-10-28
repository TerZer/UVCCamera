/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.usbcameratest9;

import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.df.lib_seete6.utils.EnginHelper;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.widget.XUSBCameraView;

public final class AutoOpenActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private XUSBCameraView cameraView;
    private USBMonitor.UsbControlBlock ctrlBlock;
    private USBMonitor mUSBMonitor;

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            mUSBMonitor.requestPermission(device);
            Log.d(TAG, "onAttach() called with: device = [" + device + "]");
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.d(TAG, "onConnect() called with: device = [" + device + "], ctrlBlock = [" + ctrlBlock + "], createNew = [" + createNew + "]");
            AutoOpenActivity.this.ctrlBlock = ctrlBlock;
            connect(null);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Log.d(TAG, "onDisconnect() called with: device = [" + device + "], ctrlBlock = [" + ctrlBlock + "]");
            cameraView.disConnect(device);
        }

        @Override
        public void onDetach(final UsbDevice device) {
            Log.d(TAG, "onDetach() called with: device = [" + device + "]");
            cameraView.disConnect(device);
        }

        @Override
        public void onCancel(final UsbDevice device) {
            Log.d(TAG, "onCancel() called with: device = [" + device + "]");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_link);
        cameraView = findViewById(R.id.xcamera_view);
        cameraView.getCaptureButton().setOnClickListener(v -> {
            cameraView.capture("/sdcard/text.png", (p) -> {
                Log.d(TAG, "capture() called with: path= [" + p + "]");
            });
        });
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        initView();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "---------onStart() called----------");
        cameraView.postDelayed(() -> {
            mUSBMonitor.addDeviceFilter(new DeviceFilter("Gkuvision Corp.", ""));
            mUSBMonitor.register();
        }, 600);

    }

    private void initView() {
        cameraView.setFrameCallback((frame, w, h) -> {
            //detect(frame, w, h);
        });

        cameraView.setOnClickListener(v -> {
            cameraView.close();
            USBCameraDialog dialog = new USBCameraDialog(AutoOpenActivity.this);
            dialog.open(ctrlBlock,AutoOpenActivity.this);
            dialog.setOnDismissListener(dialog1 -> cameraView.connect());
        });

    }


    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();
        mUSBMonitor.destroy();
        cameraView.release();
    }


    public void connect(View v) {
        if (ctrlBlock == null) return;
        if (cameraView.getCtrlBlock() != null) {
            cameraView.connect();
            return;
        }
        cameraView.connect(ctrlBlock, AutoOpenActivity.this);
    }

    public void onStartClick(View view) {
        cameraView.start();
    }

    public void onCloseClick(View view) {
        cameraView.close();
    }

    public void onStopClick(View view) {
        cameraView.stop();
    }

    public void onPauseClick(View view) {
        cameraView.pause();
    }

    public void onResumeClick(View view) {
        cameraView.resume();
    }
}
