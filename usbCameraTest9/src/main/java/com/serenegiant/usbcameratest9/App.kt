package com.serenegiant.usbcameratest9

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class App : Application() {
    class TQWinApplication: Application() {
        override fun attachBaseContext(base: Context?) {
            super.attachBaseContext(base)
            MultiDex.install(this)
        }
    }
}