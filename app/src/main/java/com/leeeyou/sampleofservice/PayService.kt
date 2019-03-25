package com.leeeyou.sampleofservice

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log

class PayService : Service() {

    val TAG = PayService::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun startService(service: Intent?): ComponentName? {
        Log.e(TAG, "startService")
        return super.startService(service)
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        Log.e(TAG, "bindService")
        return super.bindService(service, conn, flags)
    }

    override fun unbindService(conn: ServiceConnection) {
        Log.e(TAG, "unbindService")
        super.unbindService(conn)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.e(TAG, "onBind")
        return IBind()
//        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.e(TAG, "onUnbind")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
    }

    private inner class IBind : Binder(), IPayService {
        override fun pay(p: Person) {
            Log.e(TAG, "支付成功...$p")
        }

        override fun collection() {
            Log.e(TAG, "连接成功...")
        }
    }

    override fun onRebind(intent: Intent) {
        Log.e(TAG, "onRebind")
        super.onRebind(intent)
    }

}
