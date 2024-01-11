package com.serenegiant

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import com.serenegiant.usb.DeviceFilter
import com.serenegiant.usb.USBMonitor

object USBMonitorManager {
    private const val TAG = "USBMonitorManager"
    private var mUSBMonitor: USBMonitor? = null
    private val listenerList = hashSetOf<USBMonitor.OnDeviceConnectListener>()
    var attachedDevice: UsbDevice? = null
        private set

    var isRegistered = false
        private set

    var isPrintLog = false

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
        if (isPrintLog)
            Log.d(TAG, "rmAllListener() called")
        listenerList.clear()
    }

    fun requestPermission(device: UsbDevice? = attachedDevice) {
        if (device == null) return
        if (isPrintLog)
            Log.d(TAG, "requestPermission() called with: device = ${device.deviceName}")
        mUSBMonitor?.requestPermission(device)
    }


    fun register(ct: Context, filters: List<DeviceFilter>? = null) {
        if (isPrintLog) Log.d(
            TAG,
            "register() called with: ct = ${ct.javaClass.simpleName}, filters = $filters"
        )

        val mOnDeviceConnectListener: USBMonitor.OnDeviceConnectListener =
            object : USBMonitor.OnDeviceConnectListener {
                override fun onAttach(device: UsbDevice) {
                    if (isPrintLog) Log.d(TAG, "onAttach() called with: device = $device")
                    attachedDevice = device
                    listenerList.forEach { it.onAttach(device) }
                }

                override fun onDetach(device: UsbDevice) {
                    if (isPrintLog) Log.d(TAG, "onDetach() called with: device = $device")
                    attachedDevice = null
                    listenerList.forEach { it.onDetach(device) }
                }

                override fun onConnect(
                    device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean
                ) {
                    if (isPrintLog) {
                        Log.d(
                            TAG,
                            "onConnect() called with: device = $device, ctrlBlock = $ctrlBlock, createNew = $createNew"
                        )
                    }
                    listenerList.forEach { it.onConnect(device, ctrlBlock, createNew) }
                }

                override fun onDisconnect(
                    device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock
                ) {
                    if (isPrintLog) {
                        Log.d(
                            TAG,
                            "onDisconnect() called with: device = $device, ctrlBlock = $ctrlBlock"
                        )
                    }
                    listenerList.forEach { it.onDisconnect(device, ctrlBlock) }
                }

                override fun onCancel(device: UsbDevice) {
                    if (isPrintLog) {
                        Log.d(TAG, "onCancel() called with: device = $device")
                    }
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
        if (isPrintLog) Log.d(TAG, "unregister() called")
        listenerList.clear()
        mUSBMonitor?.unregister()
        isRegistered = false
    }

}