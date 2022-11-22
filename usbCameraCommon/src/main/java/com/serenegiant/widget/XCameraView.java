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

import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.CameraCallback;
import com.serenegiant.usbcameracommon.CameraFrameCallback;
import com.serenegiant.usbcameracommon.CaptureStillListener;
import com.serenegiant.usbcameracommon.R;
import com.serenegiant.usbcameracommon.UVCCameraHandler;

import java.nio.ByteBuffer;
import java.util.List;

public class XCameraView extends LinearLayout {
    private static final String TAG = "XCameraView";
    private UVCCameraHandler mHandler;
    private CameraViewInterface mUVCCameraView;
    private final ImageButton mCaptureButton;
    private CameraFrameCallback frameCallback;
    private CameraCallback cameraCallback;
    private CaptureStillListener captureStillListener;

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

    public void setCameraCallback(CameraCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    public void setCaptureStillListener(CaptureStillListener captureStillListener) {
        this.captureStillListener = captureStillListener;
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
                    if (cameraCallback != null) {
                        cameraCallback.onOpen();
                    }
                }

                @Override
                public void onClose() {
                    Log.d(TAG, "onClose() called");
                    if (cameraCallback != null) {
                        cameraCallback.onClose();
                    }
                }

                @Override
                public void onStartPreview() {
                    if (cameraCallback != null) {
                        cameraCallback.onStartPreview();
                    }
                    Log.d(TAG, "onStartPreview() called");
                }

                @Override
                public void onStopPreview() {
                    Log.d(TAG, "onStopPreview() called");
                    if (cameraCallback != null) {
                        cameraCallback.onStopPreview();
                    }
                }

                @Override
                public void onStartRecording() {
                    Log.d(TAG, "onStartRecording() called");
                    if (cameraCallback != null) {
                        cameraCallback.onStartRecording();
                    }
                }

                @Override
                public void onStopRecording() {
                    Log.d(TAG, "onStopRecording() called");
                    if (cameraCallback != null) {
                        cameraCallback.onStopRecording();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onError() called with: e = [" + e + "]");

                    if (cameraCallback != null) {
                        cameraCallback.onError(e);
                    }
                }

                @Override
                public void onCaptureFinish(String path) {
                    Log.d(TAG, "onCaptureFinish() called with: path = [" + path + "]");
                    if (cameraCallback != null) {
                        cameraCallback.onCaptureFinish(path);
                    }
                    if (captureStillListener != null) {
                        captureStillListener.onFinish(path);
                    }
                }

                @Override
                public void onFrame(ByteBuffer frame, int w, int h) {
                    if (cameraCallback != null) {
                        cameraCallback.onFrame(frame, w, h);
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
            mCaptureButton.post(new Runnable() {
                @Override
                public void run() {
                    mHandler.close();
                    setCameraButton();
                }
            });
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

    public void capture(String path) {
        mHandler.captureStill(path);
    }

    public void capture(String path, CaptureStillListener captureStillListener) {
        this.captureStillListener = captureStillListener;
        mHandler.captureStill(path);
    }

}
