package com.serenegiant.usbcameratest9

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.serenegiant.DefOnDeviceConnectListener
import com.serenegiant.USBMonitorManager
import com.serenegiant.usb.USBMonitor

class TestActivity : AppCompatActivity() {


    private var dialog: USBCameraDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        USBMonitorManager.register(this)

    }

    fun onOpenInDialog(view: View?) {

        USBMonitorManager.addListener(object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) {
            }

            override fun onDetach(device: UsbDevice?) {
            }

            override fun onConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock,
                createNew: Boolean
            ) {
                runOnUiThread {
                    dialog?.dismiss()
                    dialog = USBCameraDialog(this@TestActivity)
                    dialog?.open(ctrlBlock, this@TestActivity)
                }
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            }

            override fun onCancel(device: UsbDevice?) {
            }
        })

        USBMonitorManager.attachedDevice?.let {
            USBMonitorManager.requestPermission(it)
        }

    }

    fun onOpenInActivity(view: View?) {
        startActivity(Intent(this, USBCameraActivity::class.java))
    }
}