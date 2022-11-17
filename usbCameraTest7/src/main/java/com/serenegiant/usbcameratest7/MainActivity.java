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

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.CameraCallback;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */
public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MainActivity";

    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f, 0.5f, 0.5f};

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private UVCCameraHandler mHandlerR;
    private CameraViewInterface mUVCCameraViewR;
    private ImageButton mCaptureButtonR;

    private UVCCameraHandler mHandlerL;
    private CameraViewInterface mUVCCameraViewL;
    private ImageButton mCaptureButtonL;

    private UVCCameraHandler mHandlerLB;
    private CameraViewInterface mUVCCameraViewLB;
    private ImageButton mCaptureButtonLB;

    private UVCCameraHandler mHandlerRB;
    private CameraViewInterface mUVCCameraViewRB;
    private ImageButton mCaptureButtonRB;

    private int clickedViewId;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCamera();
        InputManager inputManager = (InputManager) this.getSystemService(INPUT_SERVICE);
        int[] devices = inputManager.getInputDeviceIds();
        Log.d(TAG, "onCreate() called with: devices.length = [" + devices.length + "]");
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        Log.d(TAG, "onCreate() called with: getDeviceCount = [" + mUSBMonitor.getDeviceCount() + "]");
        Log.d(TAG, "onCreate() called with: getDeviceList = [" + mUSBMonitor.getDeviceList() + "]");

        findViewById(R.id.textView1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MultiCameraActivity.class));
                finish();
            }
        });

    }

    private void initCamera() {

        mUVCCameraViewL = findViewById(R.id.camera_view_L);
        mUVCCameraViewL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCaptureButtonL = findViewById(R.id.capture_button_L);
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mHandlerL = UVCCameraHandler.createHandler(this, mUVCCameraViewL, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);
        mHandlerL.setName("L");
        mUVCCameraViewL.setName("L");

        mUVCCameraViewR = findViewById(R.id.camera_view_R);
        mUVCCameraViewR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCaptureButtonR = findViewById(R.id.capture_button_R);
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        mHandlerR = UVCCameraHandler.createHandler(this, mUVCCameraViewR, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1]);
        mHandlerR.setName("R");
        mUVCCameraViewR.setName("R");

        addCallback(mHandlerL);
        addCallback(mHandlerR);

        mUVCCameraViewLB = findViewById(R.id.camera_view_LB);
        mUVCCameraViewLB.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCaptureButtonLB = findViewById(R.id.capture_button_LB);
        mCaptureButtonLB.setVisibility(View.INVISIBLE);
        mHandlerLB = UVCCameraHandler.createHandler(this, mUVCCameraViewLB, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[2]);
        mHandlerLB.setName("LB");
        mUVCCameraViewLB.setName("LB");

        mUVCCameraViewRB = findViewById(R.id.camera_view_RB);
        mUVCCameraViewRB.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCaptureButtonRB = findViewById(R.id.capture_button_RB);
        mCaptureButtonRB.setVisibility(View.INVISIBLE);
        mHandlerRB = UVCCameraHandler.createHandler(this, mUVCCameraViewRB, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[3]);
        mHandlerRB.setName("RB");
        mUVCCameraViewRB.setName("RB");
    }

    private void addCallback(UVCCameraHandler handler) {
        handler.addCallback(new CameraCallback() {
            @Override
            public void onOpen() {
                UVCCamera uvcCamera = handler.getUVCCamera();
                if (uvcCamera != null) {
                    List<Size> supportedSizeList = uvcCamera.getSupportedSizeList();
                    Size maxSize = supportedSizeList.get(supportedSizeList.size() - 1);
                    uvcCamera.setPreviewSize(maxSize.width, maxSize.height);
                }
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
                Log.e(TAG, "onError() called with: e = [" + e + "]");
            }

            @Override
            public void onFrame(ByteBuffer frame, int w, int h) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onResume();

        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onResume();

        if (mUVCCameraViewRB != null)
            mUVCCameraViewRB.onResume();
        if (mUVCCameraViewLB != null)
            mUVCCameraViewLB.onResume();
    }

    @Override
    protected void onStop() {
        mHandlerR.close();
        mHandlerL.close();

        mHandlerRB.close();
        mHandlerLB.close();

        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onPause();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onPause();

        if (mUVCCameraViewRB != null)
            mUVCCameraViewRB.onPause();
        if (mUVCCameraViewLB != null)
            mUVCCameraViewLB.onPause();

        mCaptureButtonR.setVisibility(View.INVISIBLE);
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mCaptureButtonRB.setVisibility(View.INVISIBLE);
        mCaptureButtonLB.setVisibility(View.INVISIBLE);

        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerR != null) {
            mHandlerR = null;
        }
        if (mHandlerL != null) {
            mHandlerL = null;
        }

        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewR = null;
        mCaptureButtonR = null;
        mUVCCameraViewL = null;
        mCaptureButtonL = null;
        mUVCCameraViewRB = null;
        mCaptureButtonRB = null;
        mUVCCameraViewLB = null;
        mCaptureButtonLB = null;
        super.onDestroy();
    }


    public void onCameraViewClick(final View view) {
        clickedViewId = view.getId();
        switch (clickedViewId) {
            case R.id.camera_view_L:
                shoeSelectDialog(mHandlerL);
                break;
            case R.id.camera_view_R:
                shoeSelectDialog(mHandlerR);
                break;
            case R.id.camera_view_LB:
                shoeSelectDialog(mHandlerLB);
                break;
            case R.id.camera_view_RB:
                shoeSelectDialog(mHandlerRB);
                break;
        }
    }

    public void onCaptureViewClick(final View view) {
        switch (clickedViewId) {
            case R.id.capture_button_L:
                capture(mHandlerL, mCaptureButtonL);
                break;

            case R.id.capture_button_R:
                capture(mHandlerR, mCaptureButtonR);
                break;

            case R.id.capture_button_LB:
                capture(mHandlerLB, mCaptureButtonLB);
                break;

            case R.id.capture_button_RB:
                capture(mHandlerRB, mCaptureButtonRB);
                break;
        }
    }


    private void shoeSelectDialog(UVCCameraHandler cameraHandler) {
        if (cameraHandler == null) {
            return;
        }
        if (!cameraHandler.isOpened()) {
            CameraDialog.showDialog(MainActivity.this);
        } else {
            cameraHandler.close();
            setCameraButton();
        }
    }

    private void capture(UVCCameraHandler cameraHandler, ImageButton imageButton) {
        if (cameraHandler != null) {
            if (cameraHandler.isOpened()) {
                if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                    if (!cameraHandler.isRecording()) {
                        imageButton.setColorFilter(0xffff0000);    // turn red
                        cameraHandler.startRecording();
                    } else {
                        imageButton.setColorFilter(0);    // return to default color
                        cameraHandler.stopRecording();
                    }
                }
            }
        }
    }

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:" + device);
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:" + device);
            switch (clickedViewId) {
                case R.id.camera_view_L:
                    connect(ctrlBlock, mHandlerL, mUVCCameraViewL, mCaptureButtonL);
                    break;
                case R.id.camera_view_R:
                    connect(ctrlBlock, mHandlerR, mUVCCameraViewR, mCaptureButtonR);
                    break;

                case R.id.camera_view_LB:
                    connect(ctrlBlock, mHandlerLB, mUVCCameraViewLB, mCaptureButtonLB);
                    break;
                case R.id.camera_view_RB:
                    connect(ctrlBlock, mHandlerRB, mUVCCameraViewRB, mCaptureButtonRB);
                    break;
            }


        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device);
            switch (clickedViewId) {
                case R.id.camera_view_L:
                    disConnect(device, mHandlerL);
                    break;
                case R.id.camera_view_R:
                    disConnect(device, mHandlerR);
                    break;
                case R.id.camera_view_LB:
                    disConnect(device, mHandlerLB);
                    break;
                case R.id.camera_view_RB:
                    disConnect(device, mHandlerRB);
                    break;
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device);
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:");
        }
    };

    private void connect(UsbControlBlock ctrlBlock, UVCCameraHandler handler, CameraViewInterface cameraViewInterface, ImageButton button) {
        if (handler.isOpened()) {
            return;
        }

        handler.open(ctrlBlock);
        final SurfaceTexture st = cameraViewInterface.getSurfaceTexture();
        handler.startPreview(new Surface(st));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void disConnect(UsbDevice device, UVCCameraHandler handler) {
        if ((handler != null) && !handler.isEqual(device)) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    handler.close();
                    setCameraButton();
                }
            }, 0);
        }
    }

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
                    setCameraButton();
                }
            }, 0);
        }
    }

    private void setCameraButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mHandlerL != null) && !mHandlerL.isOpened() && (mCaptureButtonL != null)) {
                    mCaptureButtonL.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerR != null) && !mHandlerR.isOpened() && (mCaptureButtonR != null)) {
                    mCaptureButtonR.setVisibility(View.INVISIBLE);
                }

                if ((mHandlerLB != null) && !mHandlerLB.isOpened() && (mCaptureButtonLB != null)) {
                    mCaptureButtonL.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerRB != null) && !mHandlerRB.isOpened() && (mCaptureButtonRB != null)) {
                    mCaptureButtonRB.setVisibility(View.INVISIBLE);
                }
            }
        }, 0);
    }
}
