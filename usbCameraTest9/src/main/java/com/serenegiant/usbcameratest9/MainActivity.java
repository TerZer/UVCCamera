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

import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.df.lib_seete6.PresenterImpl;
import com.df.lib_seete6.SeetaContract;
import com.df.lib_seete6.Target;
import com.df.lib_seete6.config.EnginConfig;
import com.df.lib_seete6.utils.EnginHelper;
import com.df.lib_seete6.view.FaceRectView;
import com.serenegiant.YUVToBitmap;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.widget.XUSBCameraView;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.nio.ByteBuffer;

public final class MainActivity extends AppCompatActivity implements SeetaContract.ViewInterface {
    private static final String TAG = "MainActivity";
    private PresenterImpl presenter;
    private FaceRectView faceRectView;
    private XUSBCameraView cameraView;
    private ImageView ivPic;
    private YUVToBitmap nv21ToBitmap;
    private USBMonitor.UsbControlBlock ctrlBlock;
    private USBMonitor mUSBMonitor;
    private boolean isNeedTakePic = false;


    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            mUSBMonitor.requestPermission(device);
            Log.d(TAG, "onAttach() called with: device = [" + device + "]");
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.d(TAG, "onConnect() called with: device = [" + device + "], ctrlBlock = [" + ctrlBlock + "], createNew = [" + createNew + "]");
            MainActivity.this.ctrlBlock = ctrlBlock;
            connectUSBCamera();
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
        setContentView(R.layout.activity_main);
        ivPic = findViewById(R.id.iv_pic);
        nv21ToBitmap = new YUVToBitmap(this);
        faceRectView = findViewById(R.id.face_rect_view);
        cameraView = findViewById(R.id.xcamera_view);
        cameraView.getCaptureButton().setOnClickListener(v -> {
            isNeedTakePic = true;
            if (presenter != null) {
                presenter.takePicture("/sdcard/", "uvc.jpg");
            }
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
        initFaceEngine();
        faceRectView.postDelayed(() -> {
            mUSBMonitor.register();
        }, 1000);
    }

    private void initView() {
        cameraView.setFrameCallback((frame, w, h) -> {
            //detect(frame, w, h);
        });
    }


    private void detect(ByteBuffer frame, int w, int h) {
        int len = frame.capacity();
        byte[] nv12 = new byte[len];
        frame.get(nv12);
        if (isNeedTakePic) {
            Bitmap cameraBitmap = nv21ToBitmap.YUV2Bitmap(nv12, w, h);
            ivPic.setImageBitmap(cameraBitmap);
            isNeedTakePic = false;
        }
        presenter.detect(nv12, w, h, 0);
    }


    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();
        mUSBMonitor.destroy();
        cameraView.release();
        EnginHelper.getInstance().release();
    }


    @Override
    public void drawFaceRect(Rect rect) {
        faceRectView.drawFaceRect(rect, 1, 1);
    }

    @Override
    public void drawFaceImage(Bitmap bitmap) {

    }

    @Override
    public boolean isActive() {
        return !isDestroyed() || !isFinishing();
    }

    @Override
    public void onOpenCameraError(int i, String s) {

    }


    @Override
    public void onDetectFinish(Target target, Mat mat, Rect rect) {

    }

    @Override
    public void onRegisterByFrameFaceFinish(boolean b, String s) {

    }

    @Override
    public void onTakePictureFinish(String path, String name) {
        boolean ret = EnginHelper.getInstance().registerFace("UVC", new File(path + name));
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "register face ret=" + ret, Toast.LENGTH_LONG).show());
        Log.d(TAG, "onTakePictureFinish() called with: path = [" + path + "], name = [" + name + "],register ret=" + ret);
    }


    private void initFaceEngine() {
        presenter = new PresenterImpl(this);
        new Thread(() -> {
            EnginConfig enginConfig = new EnginConfig();
            enginConfig.isNeedFlipLeftToRight = false;
            EnginHelper.getInstance().initEngine(MainActivity.this, enginConfig);
        }).start();

    }

    private void connectUSBCamera() {
        if (ctrlBlock == null) return;
        cameraView.connect(ctrlBlock, MainActivity.this);
    }
}
