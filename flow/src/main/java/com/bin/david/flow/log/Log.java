package com.bin.david.flow.log;

import static com.bin.david.flow.flow.Flow.LOG_ENABLE;

public class Log {

    public static void w(String message){
        if(LOG_ENABLE) {
            android.util.Log.w("flow", message);
        }
    }
}
