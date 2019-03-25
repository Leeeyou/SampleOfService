package com.leeeyou.sampleofservice.book

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.leeeyou.sampleofservice.R
import kotlinx.android.synthetic.main.activity_book_manager.*

class BookManagerActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "BookManagerActivity"
        private const val MESSAGE_NEW_BOOK_ARRIVED = 1
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_NEW_BOOK_ARRIVED -> Log.d(TAG, "receive new book :" + msg.obj)
                else -> super.handleMessage(msg)
            }
        }
    }

    private var mRemoteBookManagerBinder: IBookManager? = null

    private val mDeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.d(TAG, "[binder died] Current thread name : " + Thread.currentThread().name)
            if (mRemoteBookManagerBinder == null) {
                return
            }
            mRemoteBookManagerBinder!!.asBinder().unlinkToDeath(this, 0)
            mRemoteBookManagerBinder = null
            // TODO:这里重新绑定远程Service
        }
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "conn onServiceConnected")
            mRemoteBookManagerBinder = IBookManager.Stub.asInterface(service)
            try {
                //link to death
                mRemoteBookManagerBinder!!.asBinder().linkToDeath(mDeathRecipient, 0)

                val list = mRemoteBookManagerBinder!!.bookList
                Log.d(TAG, "query book list, list type:" + list.javaClass.canonicalName!!)
                Log.d(TAG, "query book list:$list")

                val newBook = Book(3, "Android进阶")
                mRemoteBookManagerBinder!!.addBook(newBook)
                Log.d(TAG, "add book:$newBook")

                val newList = mRemoteBookManagerBinder!!.bookList
                Log.d(TAG, "query book list:$newList")

                mRemoteBookManagerBinder!!.registerListener(mOnNewBookArrivedListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

        }

        override fun onServiceDisconnected(className: ComponentName) {
            mRemoteBookManagerBinder = null
            Log.d(TAG, "[onServiceDisconnected] Current thread name : " + Thread.currentThread().name)
        }
    }

    private val mOnNewBookArrivedListener = object : IOnNewBookArrivedListener.Stub() {
        @Throws(RemoteException::class)
        override fun onNewBookArrived(newBook: Book) {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_manager)
        val intent = Intent(this, BookManagerService::class.java)
        val b = bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        Log.d(TAG, "bind BookManagerService result is ：$b")

        button1.setOnClickListener {
            Toast.makeText(this, "click button", Toast.LENGTH_SHORT).show()
            //在子线程中处理耗时操作
            Thread(Runnable {
                if (mRemoteBookManagerBinder != null) {
                    try {
                        val newList = mRemoteBookManagerBinder!!.bookList
                        Log.i(TAG, "Total number of books is :" + newList.size)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }

                }
            }).start()
        }
    }

    override fun onDestroy() {
        //is binder alive
        if (mRemoteBookManagerBinder != null && mRemoteBookManagerBinder!!.asBinder().isBinderAlive) {
            try {
                Log.d(TAG, "unregister listener:$mOnNewBookArrivedListener")
                mRemoteBookManagerBinder!!.unregisterListener(mOnNewBookArrivedListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        unbindService(mConnection)
        super.onDestroy()
    }


}
