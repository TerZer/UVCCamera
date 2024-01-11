package com.serenegiant.usbcameratest9

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.serenegiant.DefOnDeviceConnectListener
import com.serenegiant.USBMonitorManager
import com.serenegiant.usb.DeviceFilter
import com.serenegiant.usb.USBMonitor

class TestActivity : AppCompatActivity() {
    private val TAG = "TestActivity"
    private var dialog: USBCameraDialog? = null
    private val filter = arrayListOf<DeviceFilter>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        USBMonitorManager.isPrintLog = true

        filter.add(DeviceFilter("Gkuvision Corp.", "USB 2.0 HD1080P PC Camera"))
        USBMonitorManager.register(this, filter)
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume() called")
        super.onResume()
    }

    fun onOpenInDialog(view: View?) {
        USBMonitorManager.addListener(object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) {
                Log.d(TAG, "onAttach() called with: device = $device")
            }

            override fun onDetach(device: UsbDevice?) {
                Log.d(TAG, "onDetach() called with: device = $device")
            }

            override fun onConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock,
                createNew: Boolean
            ) {
                runOnUiThread {
                    dialog?.dismiss()
                    dialog = USBCameraDialog(this@TestActivity)
//                    dialog?.setOnDismissListener {
//                        USBMonitorManager.register(this@TestActivity)
//                    }
                    dialog?.open(ctrlBlock, this@TestActivity)
                }
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            }

            override fun onCancel(device: UsbDevice?) {
            }
        })

        view?.postDelayed({
            USBMonitorManager.attachedDevice?.let {
                USBMonitorManager.requestPermission(it)
            }
        }, 400)

    }

    fun onOpenInActivity(view: View?) {
        startActivity(Intent(this, USBCameraActivity::class.java))
    }

    fun onRegister(view: View) {
        USBMonitorManager.register(this,filter)
    }
}