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

package com.serenegiant.usbcameratest7;

import android.hardware.input.InputManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.widget.XCameraView;

import java.util.ArrayList;
import java.util.List;

/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */
public final class MultiCameraActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MultiCameraActivity";
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private int clickedViewId;
    private List<XCameraView> xCameraViews;
    private XCameraView currentView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_camera);
        initCamera();
        InputManager inputManager = (InputManager) this.getSystemService(INPUT_SERVICE);
        int[] devices = inputManager.getInputDeviceIds();
        Log.d(TAG, "onCreate() called with: devices.length = [" + devices.length + "]");
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        Log.d(TAG, "onCreate() called with: getDeviceCount = [" + mUSBMonitor.getDeviceCount() + "]");
        Log.d(TAG, "onCreate() called with: getDeviceList = [" + mUSBMonitor.getDeviceList() + "]");

    }

    private void initCamera() {
        GridLayout gridLayoutCameras = findViewById(R.id.gl_cameras);
        int count = gridLayoutCameras.getChildCount();
        xCameraViews = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            View view = gridLayoutCameras.getChildAt(i);
            if (view instanceof XCameraView) {
                view.setOnClickListener(v -> {
                    currentView = (XCameraView) v;
                    showSelectDialog(currentView);
                });
                xCameraViews.add((XCameraView) view);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
    }

    @Override
    protected void onStop() {
        mUSBMonitor.unregister();
        if (xCameraViews != null) {
            for (XCameraView view : xCameraViews) {
                view.pause();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        if (xCameraViews != null) {
            for (XCameraView view : xCameraViews) {
                view.release();
            }
        }
        super.onDestroy();
    }


    private void showSelectDialog(XCameraView xCameraView) {
        if (xCameraView == null) {
            return;
        }
        if (!xCameraView.isOpened()) {
            CameraDialog.showDialog(MultiCameraActivity.this);
        } else {
            xCameraView.pause();
        }
    }


    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:" + device);
            Toast.makeText(MultiCameraActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (currentView == null) {
                return;
            }
            currentView.connect(ctrlBlock);
            if (DEBUG) Log.v(TAG, "onConnect:" + device);

        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device);
            if (currentView == null) {
                return;
            }
            currentView.disConnect(device);

        }

        @Override
        public void onDetach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device);
            Toast.makeText(MultiCameraActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:");
        }
    };


    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            }, 0);
        }
    }

}
