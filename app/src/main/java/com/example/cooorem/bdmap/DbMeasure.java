package com.example.cooorem.bdmap;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;

/**
 * Created by Administrator on 2015/9/17.
 */
public class DbMeasure {
    private static final String LOG_TAG = "DbMeasure";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private boolean flag=true;
    private myThread thread=null;
    private double db=0;
    private int testNum=0;
    public DbMeasure(){
        AudioRecordTest();
    }

    public double getDb() {
        return db;
    }

    public void onRecord(boolean start) {
        if (start) {
            flag=true;
            startRecording();
        } else {
            if(thread!=null)
                thread.exit();
        }
    }
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();
        thread=new myThread();
        thread.start();
    }
    public void stopRecording() {
        mRecorder.release();
        mRecorder = null;
    }
    public void pauseRecording(){
        mRecorder.stop();
    }
    public void AudioRecordTest() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/recode.3gp";
    }


    private class myThread extends Thread{
        myThread(){
        }
        public void exit()
        {
            testNum=0;
            db=0;
            flag=false;
        }
        public void run(){
            while(flag){
                int x=mRecorder.getMaxAmplitude();
                if(x!=0){
                    db=20*Math.log(x)/Math.log(10);
                }
            }
        }
    }
}
