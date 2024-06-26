package com.serenegiant.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.serenegiant.Config;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.BuildConfig;
import com.serenegiant.usbcameracommon.CameraCallback;
import com.serenegiant.usbcameracommon.CameraFrameCallback;
import com.serenegiant.usbcameracommon.CaptureStillListener;
import com.serenegiant.usbcameracommon.R;
import com.serenegiant.usbcameracommon.UVCCameraHandler;

import java.nio.ByteBuffer;

public class XUSBCameraView extends FrameLayout {

    private static final boolean DEBUG = Config.DEBUG;
    private static final String TAG = "XUSBCameraView";
    private UVCCameraHandler uvcCameraHandler;
    private final UVCCameraTextureView mUVCCameraView;
    private ImageButton mCaptureButton;
    private CameraFrameCallback frameCallback;
    private CameraCallback cameraCallback;
    private CaptureStillListener captureStillListener;
    private USBMonitor.UsbControlBlock ctrlBlock;
    private Activity ownerActivity;

    private int previewHeight = UVCCamera.DEFAULT_PREVIEW_HEIGHT;
    private int previewWidth = UVCCamera.DEFAULT_PREVIEW_WIDTH;

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
        mCaptureButton = findViewById(R.id.x_btn_capture_button);
        mUVCCameraView = findViewById(R.id.x_camera_view);
        mUVCCameraView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
    }

    public ImageButton getCaptureButton() {
        if (mCaptureButton == null) {
            mCaptureButton = findViewById(R.id.x_btn_capture_button);
        }
        return mCaptureButton;
    }


    public void setPreviewSize(int previewWidth, int previewHeight) {
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
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


    public void setSurfaceCallback(CameraViewInterface.SurfaceCallback callback) {
        mUVCCameraView.setSurfaceCallback(callback);
    }

    public boolean autoOpen() {
        return ownerActivity != null;
    }


    private void createHandler() {
        if (uvcCameraHandler != null) return;
        if (ownerActivity == null || ownerActivity.isDestroyed()) {
            Log.e(TAG, "createHandler() fail,cause ownerActivity is null or is Destroyed ");
            return;
        }
        uvcCameraHandler = UVCCameraHandler.createHandler(ownerActivity, mUVCCameraView, previewWidth, previewHeight, 0.5f);
        uvcCameraHandler.addCallback(new CameraCallback() {
            @Override
            public void onOpen() {
                Log.d(TAG, "onOpen() called");
                prepareCamera();
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

    private void prepareCamera() {
        UVCCamera uvcCamera = uvcCameraHandler.getUVCCamera();
        if (uvcCamera == null) {
            Log.e(TAG, "prepareCamera() called fail,can not get uvcCamera!");
            return;
        }

        Size maxSize = uvcCamera.getMaxSize();
        if (maxSize == null) {
            Log.e(TAG, "prepareCamera() called  fail, not find max size!");
            return;
        }
        Log.i(TAG, "prepareCamera() called fin max size:" + maxSize);
        uvcCamera.setPreviewSize(maxSize.width, maxSize.height);

        if (frameCallback != null) {
            uvcCamera.setFrameCallback(frame -> {
                frameCallback.onFrame(frame, uvcCamera.getCurrentWidth(), uvcCamera.getCurrentHeight());
            }, UVCCamera.PIXEL_FORMAT_YUV420SP);
        }

        if (cameraCallback != null) {
            cameraCallback.onOpen();
        }
    }

    public void connect() {
        if (ownerActivity == null) {
            Log.e(TAG, "connect() called with fail,ownerActivity is null");
            return;
        }

        if (ctrlBlock == null) {
            Log.e(TAG, "connect() called with fail,ctrlBlock is null");
            return;
        }
        connect(ctrlBlock, ownerActivity);
    }

    public void connect(USBMonitor.UsbControlBlock ctrlBlock, Activity ownerActivity) {
        this.ownerActivity = ownerActivity;
        createHandler();
        connect(ctrlBlock, uvcCameraHandler, mUVCCameraView);
    }


    public void connect(USBMonitor.UsbControlBlock ctrlBlock) {
        createHandler();
        connect(ctrlBlock, uvcCameraHandler, mUVCCameraView);
    }

    private void connect(USBMonitor.UsbControlBlock ctrlBlock, UVCCameraHandler handler, CameraViewInterface cameraViewInterface) {
        if (!isAttachedToWindow()) {
            Log.e(TAG, "connect() called with fail,is not attached to Window");
            return;
        }

        if (BuildConfig.DEBUG)
            Log.d(TAG, "connect() called with: ctrlBlock = [" + ctrlBlock.getInfo() + "], handler = [" + handler.getName() + "]");
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
        if (st == null) {
            Log.e(TAG, "connect() called with fail,getSurfaceTexture is null");
            return;
        }
        handler.startPreview(new Surface(st));
    }

    public void disConnect(UsbDevice device) {
        Log.d(TAG, "disConnect() called with: device = [" + device + "]");
        if ((uvcCameraHandler != null) && uvcCameraHandler.isEqual(device)) {
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


    public void start(Object holder) {
        Log.d(TAG, "start() called with: holder = [" + holder + "]");
        if (holder == null) {
            return;
        }
        uvcCameraHandler.startPreview(holder);
    }


    public void start() {
        Log.d(TAG, "start() called");
        Surface holder = mUVCCameraView.getTexture();
        if (holder == null) {
            Log.e(TAG, "start() called fail,holder is noll");
            return;
        }
        start(holder);
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
        }
    }

    public void close() {
        if (uvcCameraHandler != null) {
            uvcCameraHandler.close();
        }
    }

    public void release() {
        Log.d(TAG, "release() called");
        stop();
        close();

        if (uvcCameraHandler != null) {
            uvcCameraHandler.release();
            uvcCameraHandler = null;
        }
        ownerActivity = null;
        cameraCallback = null;
        frameCallback = null;
        captureStillListener = null;
    }
}
