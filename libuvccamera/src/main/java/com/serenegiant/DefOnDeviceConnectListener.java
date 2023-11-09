package com.serenegiant;

import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.USBMonitor;

public class DefOnDeviceConnectListener implements USBMonitor.OnDeviceConnectListener {
    @Override
    public void onAttach(UsbDevice device) {

    }

    @Override
    public void onDetach(UsbDevice device) {

    }

    @Override
    public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {

    }

    @Override
    public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

    }

    @Override
    public void onCancel(UsbDevice device) {

    }
}
