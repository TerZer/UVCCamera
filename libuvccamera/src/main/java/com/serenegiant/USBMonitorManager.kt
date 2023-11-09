package com.serenegiant

import android.content.Context
import android.hardware.usb.UsbDevice
import com.serenegiant.usb.DeviceFilter
import com.serenegiant.usb.USBMonitor

object USBMonitorManager {
    private var mUSBMonitor: USBMonitor? = null
    private var isRegistered = false
    private val listenerList = hashSetOf<USBMonitor.OnDeviceConnectListener>()
    var attachedDevice: UsbDevice? = null
        private set

    fun addListener(listener: USBMonitor.OnDeviceConnectListener?, isClearAll: Boolean = true) {
        if (listener == null) return
        if (isClearAll) rmAllListener()
        listenerList.add(listener)
    }

    fun rmListener(listener: Any?) {
        if (listener == null) return
        listenerList.remove(listener)
    }

    fun rmAllListener() {
        listenerList.clear()
    }

    fun requestPermission(device: UsbDevice? = attachedDevice) {
        if (device == null) return
        mUSBMonitor?.requestPermission(device)
    }


    fun register(ct: Context, filters: List<DeviceFilter>? = null) {
        if (isRegistered) return
        val mOnDeviceConnectListener: USBMonitor.OnDeviceConnectListener =
            object : USBMonitor.OnDeviceConnectListener {
                override fun onAttach(device: UsbDevice) {
                    attachedDevice = device
                    listenerList.forEach { it.onAttach(device) }
                }

                override fun onDetach(device: UsbDevice) {
                    attachedDevice = null
                    listenerList.forEach { it.onDetach(device) }
                }

                override fun onConnect(
                    device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean
                ) {
                    listenerList.forEach { it.onConnect(device, ctrlBlock, createNew) }
                }

                override fun onDisconnect(
                    device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock
                ) {
                    listenerList.forEach { it.onDisconnect(device, ctrlBlock) }
                }

                override fun onCancel(device: UsbDevice) {
                    listenerList.forEach { it.onCancel(device) }
                }
            }

        mUSBMonitor = USBMonitor(ct.applicationContext, mOnDeviceConnectListener)
        if (!filters.isNullOrEmpty()) {
            mUSBMonitor?.addDeviceFilter(filters)
        }
        mUSBMonitor?.register()
        isRegistered = true
    }

    fun unregister() {
        listenerList.clear()
        mUSBMonitor?.unregister()
        isRegistered = false
    }

}