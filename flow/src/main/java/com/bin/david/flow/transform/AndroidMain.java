package com.bin.david.flow.transform;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Android主线程
 */
public class AndroidMain implements Converter{

    /**
     * Android主线程切换器
     */
    private static Converter androidMain;

    public static Converter get(){
        if(androidMain == null){
            synchronized (SingleThread.class) {
                if(androidMain == null) {
                    androidMain = new SingleThread();
                }
            }
        }
        return androidMain;
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void transform(Runnable runnable) {

        handler.post(() -> {
            Log.e("huang","Converter transform:"+ Thread.currentThread().getName());
            runnable.run();
        });
    }
}
