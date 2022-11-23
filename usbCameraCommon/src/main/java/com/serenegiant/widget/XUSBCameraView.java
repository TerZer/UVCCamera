package com.serenegiant.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
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

public class XUSBCameraView extends LinearLayout {
    private static final String TAG = "XCameraView";
    private UVCCameraHandler uvcCameraHandler;
    private UVCCameraTextureView mUVCCameraView;
    private final ImageButton mCaptureButton;
    private CameraFrameCallback frameCallback;
    private CameraCallback cameraCallback;
    private CaptureStillListener captureStillListener;
    private USBMonitor.UsbControlBlock ctrlBlock;
    private Activity ownerActivity;

    public XUSBCameraView(Context context) {
        super(context);
    }

    public XUSBCameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XUSBCameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XUSBCameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        View.inflate(getContext(), R.layout.layout_x_camera_view, this);
        mCaptureButton = findViewById(R.id.btn_capture_button);
        mCaptureButton.setVisibility(View.INVISIBLE);
        mUVCCameraView = findViewById(R.id.camera_view);
        mUVCCameraView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
    }

    public ImageButton getCaptureButton() {
        return mCaptureButton;
    }

    public void setOwnerActivity(Activity ownerActivity) {
        this.ownerActivity = ownerActivity;
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

    public USBMonitor.UsbControlBlock getCtrlBlock() {
        return ctrlBlock;
    }

    public void setAspectRatio(final double aspectRatio) {
        mUVCCameraView.setAspectRatio(aspectRatio);
    }

    private void createHandler() {
        if (uvcCameraHandler != null) {
            return;
        }
        if (ownerActivity == null) {
            Log.e(TAG, "createHandler() fail,cause ownerActivity is null");
            return;
        }
        uvcCameraHandler = UVCCameraHandler.createHandler(ownerActivity, mUVCCameraView, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, 0.5f);
        uvcCameraHandler.addCallback(new CameraCallback() {
            @Override
            public void onOpen() {
                UVCCamera uvcCamera = uvcCameraHandler.getUVCCamera();
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

    public void connect(USBMonitor.UsbControlBlock ctrlBlock, Activity ownerActivity) {
        this.ownerActivity = ownerActivity;
        createHandler();
        connect(ctrlBlock, uvcCameraHandler, mUVCCameraView, mCaptureButton);
    }

    public void connect(USBMonitor.UsbControlBlock ctrlBlock) {
        createHandler();
        connect(ctrlBlock, uvcCameraHandler, mUVCCameraView, mCaptureButton);
    }


    private void connect(USBMonitor.UsbControlBlock ctrlBlock, UVCCameraHandler handler, CameraViewInterface cameraViewInterface, ImageButton button) {
        Log.d(TAG, "connect() called with: ctrlBlock = [" + ctrlBlock + "], handler = [" + handler + "], cameraViewInterface = [" + cameraViewInterface + "], button = [" + button + "]");
        if (handler == null) {
            Log.e(TAG, "connect() called with fail,cause handler is null");
            return;
        }
        this.ctrlBlock = ctrlBlock;
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
        if ((uvcCameraHandler != null) && !uvcCameraHandler.isEqual(device)) {
            mCaptureButton.post(() -> {
                uvcCameraHandler.close();
                setCameraButton();
            });
        }
    }


    private void setCameraButton() {
        if ((uvcCameraHandler != null) && !uvcCameraHandler.isOpened() && (mCaptureButton != null)) {
            mCaptureButton.setVisibility(View.INVISIBLE);
        }
    }

    public void start(SurfaceHolder holder) {
        Log.d(TAG, "start() called with: holder = [" + holder + "]");
        if (uvcCameraHandler == null) {
            return;
        }
        uvcCameraHandler.startPreview(holder);
    }

    public void resume() {
        if (uvcCameraHandler == null) {
            return;
        }
        Log.d(TAG, "resume() called");
        mUVCCameraView.onResume();
    }

    public void pause() {
        Log.d(TAG, "pause() called");
        mUVCCameraView.onPause();
        mCaptureButton.setVisibility(View.INVISIBLE);
    }

    public void stop() {
        Log.d(TAG, "stop() called");
        if (uvcCameraHandler != null) {
            uvcCameraHandler.stopPreview();
            uvcCameraHandler.close();
        }
    }

    public boolean isOpened() {
        return uvcCameraHandler != null && uvcCameraHandler.isOpened();
    }


    public void capture(String path) {
        uvcCameraHandler.captureStill(path);
    }

    public void capture(String path, CaptureStillListener captureStillListener) {
        this.captureStillListener = captureStillListener;
        uvcCameraHandler.captureStill(path);
    }

    public void release() {
        Log.d(TAG, "release() called");
        if (uvcCameraHandler != null) {
            uvcCameraHandler.release();
            uvcCameraHandler = null;
        }
        mUVCCameraView.onPause();
        mUVCCameraView = null;
    }
}
