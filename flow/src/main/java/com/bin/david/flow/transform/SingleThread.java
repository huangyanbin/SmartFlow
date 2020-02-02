package com.bin.david.flow.transform;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单线程
 */
public class SingleThread implements Converter{

    /**
     *单线程池
     */
    private static Converter singleThread;

    public static Converter get(){
        if(singleThread == null){
            synchronized (SingleThread.class) {
                if(singleThread == null) {
                    singleThread = new SingleThread();
                }
            }
        }
        return singleThread;
    }
    private ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    public void transform(Runnable runnable) {
        service.submit(()->{
            Log.e("huang","Converter transform:"+ Thread.currentThread().getName());
            runnable.run();
        });
    }
}
