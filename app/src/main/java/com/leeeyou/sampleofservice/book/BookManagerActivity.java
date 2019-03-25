package com.leeeyou.sampleofservice.book;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.leeeyou.sampleofservice.R;

import java.util.List;

public class BookManagerActivity extends AppCompatActivity {

    private static final String TAG = "BookManagerActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManagerBinder;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "receive new book :" + msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "[binder died] Current thread name : " + Thread.currentThread().getName());
            if (mRemoteBookManagerBinder == null) {
                return;
            }
            mRemoteBookManagerBinder.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManagerBinder = null;
            // TODO:这里重新绑定远程Service
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "conn onServiceConnected");
            mRemoteBookManagerBinder = IBookManager.Stub.asInterface(service);
            try {
                //link to death
                mRemoteBookManagerBinder.asBinder().linkToDeath(mDeathRecipient, 0);

                List<Book> list = mRemoteBookManagerBinder.getBookList();
                Log.d(TAG, "query book list, list type:" + list.getClass().getCanonicalName());
                Log.d(TAG, "query book list:" + list.toString());

                Book newBook = new Book(3, "Android进阶");
                mRemoteBookManagerBinder.addBook(newBook);
                Log.d(TAG, "add book:" + newBook);

                List<Book> newList = mRemoteBookManagerBinder.getBookList();
                Log.d(TAG, "query book list:" + newList.toString());

                mRemoteBookManagerBinder.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mRemoteBookManagerBinder = null;
            Log.d(TAG, "[onServiceDisconnected] Current thread name : " + Thread.currentThread().getName());
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);
        Intent intent = new Intent(this, BookManagerService.class);
        boolean b = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bind BookManagerService result is ：" + b);
    }

    public void onButton1Click(View view) {
        Toast.makeText(this, "click button", Toast.LENGTH_SHORT).show();
        //在子线程中处理耗时操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mRemoteBookManagerBinder != null) {
                    try {
                        List<Book> newList = mRemoteBookManagerBinder.getBookList();
                        Log.i(TAG, "Total number of books is :" + newList.size());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        //is binder alive
        if (mRemoteBookManagerBinder != null && mRemoteBookManagerBinder.asBinder().isBinderAlive()) {
            try {
                Log.d(TAG, "unregister listener:" + mOnNewBookArrivedListener);
                mRemoteBookManagerBinder.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }

}
