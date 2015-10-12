package com.example.cooorem.bdmap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class DbService extends Service {
    private DbMeasure mDbMeasure;
    private DbBinder mBinder=new DbBinder();
    private boolean flag;
    private static MainActivity mainActivity;
    static void startSevice(Context c) {
        mainActivity = (MainActivity) c;
        Intent in = new Intent(c, DbService.class);
        c.startService(in);
    }
    static void bindService(Context c, ServiceConnection connection,int a) {
        mainActivity = (MainActivity) c;
        Intent in = new Intent(c, DbService.class);
        c.bindService(in, connection, a);
    }
    public DbService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDbMeasure=new DbMeasure();
        mDbMeasure.onRecord(true);
        flag=true;
        if(mainActivity!=null)new DBThread(mainActivity).start();
        else {
            Log.e("DbService","mainActivity = null");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("DbService","onBind");
        return mBinder;
    }
    public class DbBinder extends Binder {
        int getDb(){
            return (int)Math.round(mDbMeasure.getDb());
        }
    }

    @Override
    public void onDestroy() {
        mDbMeasure.onRecord(false);
        mDbMeasure.pauseRecording();
        mDbMeasure.stopRecording();
        flag=false;
        super.onDestroy();
    }
    class DBThread extends Thread {
        private Context context;
        private android.os.Handler handler;
        public DBThread(){

        }

        public DBThread(Context context) {
            this.context = context;
            this.handler = ((MainActivity) context).getHandler();
        }
        @Override
        public void run() {
            while(flag){
                Message msg=new Message();
                msg.what=(int)Math.round(mDbMeasure.getDb());
                handler.sendMessage(msg);
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
