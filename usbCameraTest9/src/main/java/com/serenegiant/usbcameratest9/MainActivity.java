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
import android.os.Bundle;

import com.df.lib_seete6.Contract;
import com.df.lib_seete6.PresenterImpl;
import com.df.lib_seete6.utils.EnginHelper;
import com.df.lib_seete6.view.FaceRectView;
import com.seeta.sdk.FaceAntiSpoofing;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.widget.XCameraView;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public final class MainActivity extends BaseActivity implements Contract.View {
    private PresenterImpl presenter;
    private FaceRectView faceRectView;
    private XCameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        faceRectView = findViewById(R.id.face_rect_view);
        cameraView = findViewById(R.id.xcamera_view);

        presenter = new PresenterImpl(this);
        EnginHelper.getInstance().initEngine(this, true);
        initView();
    }

    private void initView() {
        cameraView.setFrameCallback((frame, w, h) -> {
            int len = frame.capacity();
            byte[] nv21 = new byte[len];
            frame.get(nv21);
            presenter.detect(nv21, w, h, 0);
        });
    }


    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();
        EnginHelper.getInstance().release();
    }


    @Override
    public void drawFaceRect(Rect rect) {
        faceRectView.drawFaceRect(rect, 0, 0);
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
    public void onDetectFinish(FaceAntiSpoofing.Status status, float v, String s, Mat mat, Rect rect) {

    }

    @Override
    public void onRegisterByFrameFaceFinish(boolean b, String s) {

    }

    @Override
    public void onTakePictureFinish(String s, String s1) {

    }
}
