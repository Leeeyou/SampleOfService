package com.leeeyou.sampleofservice.book

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class BookManagerService : Service() {

    private val mIsServiceDestroyed = AtomicBoolean(false)

    private val mBookList = CopyOnWriteArrayList<Book>()
    // private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList =
    // new CopyOnWriteArrayList<IOnNewBookArrivedListener>();

    private val mListenerList = RemoteCallbackList<IOnNewBookArrivedListener>()

    private val mBinder = object : IBookManager.Stub() {

        @Throws(RemoteException::class)
        override fun getBookList(): List<Book> {
            //模拟耗时操作
            SystemClock.sleep(5000)
            return mBookList
        }

        @Throws(RemoteException::class)
        override fun addBook(book: Book) {
            mBookList.add(book)
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val check = checkCallingOrSelfPermission("com.leeeyou.sampleofservice.book.permission.ACCESS_BOOK_SERVICE")
            if (check == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "onTransact ，Check permission failed")
                return false
            }

            var packageName: String? = null
            val packages = packageManager.getPackagesForUid(Binder.getCallingUid())
            if (packages != null && packages.isNotEmpty()) {
                packageName = packages[0]
            }
            if (packageName == null || !packageName.startsWith("com.leeeyou.sampleofservice")) {
                Log.d(TAG, "onTransact ，Check that packageName failed : " + packageName!!)
                return false
            } else {
                Log.d(TAG, "onTransact ，Check that packageName passed : $packageName")
            }

            return super.onTransact(code, data, reply, flags)
        }

        @Throws(RemoteException::class)
        override fun registerListener(listener: IOnNewBookArrivedListener) {
            mListenerList.register(listener)
            val count = mListenerList.beginBroadcast()
            mListenerList.finishBroadcast()
            Log.d(TAG, "registerListener, current size:$count")
        }

        @Throws(RemoteException::class)
        override fun unregisterListener(listener: IOnNewBookArrivedListener) {
            val success = mListenerList.unregister(listener)

            if (success) {
                Log.d(TAG, "unregister success.")
            } else {
                Log.d(TAG, "not found, can not unregister.")
            }
            val count = mListenerList.beginBroadcast()
            mListenerList.finishBroadcast()
            Log.d(TAG, "unregisterListener, current size:$count")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        mBookList.add(Book(1, "Android"))
        mBookList.add(Book(2, "Ios"))
        Thread(ServiceWorker()).start()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind")
        val check = checkCallingOrSelfPermission("com.leeeyou.sampleofservice.book.permission.ACCESS_BOOK_SERVICE")
        return if (check == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "onBind ，Check permission failed")
            null
        } else {
            Log.d(TAG, "onBind ，Check permission passed")
            mBinder
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mIsServiceDestroyed.set(true)
        super.onDestroy()
    }

    @Throws(RemoteException::class)
    private fun onNewBookArrived(book: Book) {
        mBookList.add(book)
        //beginBroadcast 与 finishBroadcast 要配套使用
        val count = mListenerList.beginBroadcast()
        for (i in 0 until count) {
            val listener = mListenerList.getBroadcastItem(i)
            if (listener != null) {
                try {
                    listener.onNewBookArrived(book)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }
        mListenerList.finishBroadcast()
    }

    private inner class ServiceWorker : Runnable {
        override fun run() {
            // do background processing here.....
            while (!mIsServiceDestroyed.get()) {
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                val bookId = mBookList.size + 1
                val newBook = Book(bookId, "new book#$bookId")
                try {
                    onNewBookArrived(newBook)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
        }
    }

    companion object {

        private val TAG = BookManagerService::class.java.simpleName
    }

}
