package com.serenegiant.usbcameracommon;

import java.nio.ByteBuffer;

public interface CameraFrameCallback {
    void onFrame(ByteBuffer frame, int w, int h);
}
