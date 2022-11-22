package com.serenegiant.usbcameracommon;

import java.nio.ByteBuffer;

public interface CameraCallback {
    void onOpen();

    void onClose();

    void onStartPreview();

    void onStopPreview();

    void onStartRecording();

    void onStopRecording();

    void onError(final Exception e);

    void onFrame(ByteBuffer frame, int w, int h);

    void onCaptureFinish(String path);

}
