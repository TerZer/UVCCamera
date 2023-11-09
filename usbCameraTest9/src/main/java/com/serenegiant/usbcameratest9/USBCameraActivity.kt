package com.serenegiant.usbcameratest9

import android.hardware.usb.UsbDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import com.serenegiant.USBMonitorManager
import com.serenegiant.usb.USBMonitor
import com.serenegiant.widget.CameraViewInterface
import com.serenegiant.widget.XUSBCameraView

class USBCameraActivity : AppCompatActivity() {
    private var xcv: XUSBCameraView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usbcamrea)
        initView()
    }

    private fun initView() {
        xcv = findViewById(R.id.test_camera_view)
        USBMonitorManager.addListener(object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) {
            }

            override fun onDetach(device: UsbDevice?) {
            }

            override fun onConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?,
                createNew: Boolean
            ) {
                runOnUiThread { xcv?.connect(ctrlBlock, this@USBCameraActivity) }
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            }

            override fun onCancel(device: UsbDevice?) {
            }
        })


        xcv?.setSurfaceCallback(object : CameraViewInterface.SurfaceCallback {
            override fun onSurfaceCreated(view: CameraViewInterface?, surface: Surface?) {
                USBMonitorManager.attachedDevice?.run {
                    USBMonitorManager.requestPermission(this)
                }
            }

            override fun onSurfaceChanged(
                view: CameraViewInterface?,
                surface: Surface?,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceDestroy(view: CameraViewInterface?, surface: Surface?) {
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        xcv?.release()
    }


}