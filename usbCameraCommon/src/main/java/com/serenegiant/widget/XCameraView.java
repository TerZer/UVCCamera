package com.serenegiant.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.CameraCallback;
import com.serenegiant.usbcameracommon.CameraFrameCallback;
import com.serenegiant.usbcameracommon.R;
import com.serenegiant.usbcameracommon.UVCCameraHandler;

import java.nio.ByteBuffer;
import java.util.List;

public class XCameraView extends LinearLayout {
    private static final String TAG = "XCameraView";
    private UVCCameraHandler mHandler;
    private CameraViewInterface mUVCCameraView;
    private final ImageButton mCaptureButton;
    private final BaseActivity baseActivity;
    private CameraFrameCallback frameCallback;

    public XCameraView(Context context) {
        super(context);
    }

    public XCameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XCameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XCameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        baseActivity = (BaseActivity) getContext();
        View.inflate(getContext(), R.layout.layout_x_camera_view, this);
        mCaptureButton = findViewById(R.id.btn_capture_button);
        mCaptureButton.setVisibility(View.INVISIBLE);
        mUVCCameraView = findViewById(R.id.camera_view);
        mUVCCameraView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        createHandler();
    }

    public ImageButton getCaptureButton() {
        return mCaptureButton;
    }

    public void setFrameCallback(CameraFrameCallback frameCallback) {
        this.frameCallback = frameCallback;
    }

    private void createHandler() {
        Context context = getContext();
        if (context instanceof Activity) {
            mHandler = UVCCameraHandler.createHandler((Activity) context, mUVCCameraView, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, 0.5f);
            mHandler.addCallback(new CameraCallback() {
                @Override
                public void onOpen() {
                    UVCCamera uvcCamera = mHandler.getUVCCamera();
                    if (uvcCamera != null) {
                        List<Size> supportedSizeList = uvcCamera.getSupportedSizeList();
                        Size maxSize = supportedSizeList.get(supportedSizeList.size() - 1);
                        uvcCamera.setPreviewSize(maxSize.width, maxSize.height);
                        if (frameCallback != null) {
                            uvcCamera.setFrameCallback(frame -> {
                                frameCallback.onFrame(frame, uvcCamera.getCurrentWidth(), uvcCamera.getCurrentHeight());
                            }, UVCCamera.PIXEL_FORMAT_YUV420SP);
                        }
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
                    if (frameCallback != null) {
                        frameCallback.onFrame(frame, w, h);
                    }
                }
            });
        }
    }


    public void connect(USBMonitor.UsbControlBlock ctrlBlock) {
        connect(ctrlBlock, mHandler, mUVCCameraView, mCaptureButton);
    }


    private void connect(USBMonitor.UsbControlBlock ctrlBlock, UVCCameraHandler handler, CameraViewInterface cameraViewInterface, ImageButton button) {
        Log.d(TAG, "connect() called with: ctrlBlock = [" + ctrlBlock + "], handler = [" + handler + "], cameraViewInterface = [" + cameraViewInterface + "], button = [" + button + "]");
        if (handler.isOpened()) {
            return;
        }
        handler.open(ctrlBlock);
        final SurfaceTexture st = cameraViewInterface.getSurfaceTexture();
        handler.startPreview(new Surface(st));
        post(() -> button.setVisibility(View.VISIBLE));
    }


    public void disConnect(UsbDevice device) {
        Log.d(TAG, "disConnect() called with: device = [" + device + "]");
        if ((mHandler != null) && !mHandler.isEqual(device)) {
            baseActivity.queueEvent(() -> {
                mHandler.close();
                setCameraButton();
            }, 0);
        }
    }


    private void setCameraButton() {
        if ((mHandler != null) && !mHandler.isOpened() && (mCaptureButton != null)) {
            mCaptureButton.setVisibility(View.INVISIBLE);
        }
    }


    public void resume() {
        Log.d(TAG, "resume() called");
        mUVCCameraView.onResume();
    }

    public void pause() {
        Log.d(TAG, "pause() called");
        mHandler.close();
        mUVCCameraView.onPause();
        mCaptureButton.setVisibility(View.INVISIBLE);
    }


    public void release() {
        Log.d(TAG, "release() called");
        mHandler.release();
        mUVCCameraView.onPause();
        mUVCCameraView = null;
        mHandler = null;
    }

    public boolean isOpened() {
        return mHandler.isOpened();
    }

    public void capture() {
        capture(mHandler, mCaptureButton);
    }


    private void capture(UVCCameraHandler cameraHandler, ImageButton imageButton) {
        if (cameraHandler != null) {
            if (cameraHandler.isOpened()) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) getContext();
                if (BaseActivity.checkPermissionWriteExternalStorage(appCompatActivity) && BaseActivity.checkPermissionAudio(appCompatActivity)) {
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


}
