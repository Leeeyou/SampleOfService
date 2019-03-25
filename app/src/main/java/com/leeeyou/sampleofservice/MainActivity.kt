package com.leeeyou.sampleofservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName

    private lateinit var mIntent: Intent
    private var ps: IPayService? = null
    private var isBoundForConn = false

    private lateinit var mIntent2: Intent
    private var isBoundForConn2 = false
    private var ps2: IPayService? = null

    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.e(TAG, "conn onServiceDisconnected $name")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.e(TAG, "conn onServiceConnected $name")
            ps = service as IPayService
        }
    }

    private val conn2 = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.e(TAG, "conn2 onServiceDisconnected $name")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.e(TAG, "conn2 onServiceConnected $name")
            ps2 = service as IPayService
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mIntent = Intent(this@MainActivity, PayService::class.java)
        firstClient()

        mIntent2 = Intent(this@MainActivity, PayService::class.java)
        secondClient()
    }

    private fun firstClient() {
        btn_bind.setOnClickListener { isBoundForConn = bindService(mIntent, conn, Context.BIND_AUTO_CREATE) }
        btn_unbind.setOnClickListener {
            if (isBoundForConn) {
                unbindService(conn)
                isBoundForConn = false
                ps = null
            } else {
                Log.e(TAG, "conn is unbind")
            }
        }
        btn_invoke_remote_service.setOnClickListener {
            val p = Person("Mark", 29)
            ps?.pay(p)
        }
    }

    private fun secondClient() {
        btn_bind2.setOnClickListener { isBoundForConn2 = bindService(mIntent2, conn2, Context.BIND_AUTO_CREATE) }
        btn_unbind2.setOnClickListener {
            if (isBoundForConn2) {
                unbindService(conn2)
                isBoundForConn2 = false
                ps2 = null
            } else {
                Log.e(TAG, "conn2 is unbind")
            }
        }
        btn_invoke_remote_service2.setOnClickListener {
            val p = Person("Lily", 22)
            ps2?.pay(p)
        }
    }

    fun clickStart(v: View) {
        startService(mIntent)
    }

    fun clickStop(v: View) {
        stopService(mIntent)
    }

}
