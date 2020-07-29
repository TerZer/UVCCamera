package com.serenegiant.usbcameratest7.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.CameraCallback;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameratest7.R;
import com.serenegiant.widget.CameraViewInterface;

import java.util.List;

public class XCameraView extends LinearLayout {
    private static final String TAG = "XCameraView";
    private View contentView;
    private UVCCameraHandler mHandler;
    private CameraViewInterface mUVCCameraView;
    private ImageButton mCaptureButton;
    private BaseActivity baseActivity;

    public XCameraView(Context context) {
        super(context);
    }

    public XCameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XCameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                        if (maxSize.width == 1600) {
                            uvcCamera.setPreviewSize(640, 480);
                        } else {
                            uvcCamera.setPreviewSize(maxSize.width, maxSize.height);
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
        post(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(View.VISIBLE);
            }
        });
    }


    public void disConnect(UsbDevice device) {
        Log.d(TAG, "disConnect() called with: device = [" + device + "]");
        if ((mHandler != null) && !mHandler.isEqual(device)) {
            baseActivity.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mHandler.close();
                    setCameraButton();
                }
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
