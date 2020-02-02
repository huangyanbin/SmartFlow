package com.bin.david.flow.exception;

import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * 流自定义异常
 */
public class FlowException extends Exception {

    private int code;
    private Object tag;

    public FlowException(String message) {
        super(message);
    }

    public FlowException(int code,String message) {
        super(message);
        this.code = code;
    }

    public FlowException(int code, Object tag) {
        this.code = code;
        this.tag = tag;
    }

    public FlowException(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public FlowException(Throwable cause) {
        super(cause);
    }

    public int getCode() {
        return code;
    }

    @NonNull
    @Override
    public String getMessage() {
        String message =  super.getMessage();
        if(TextUtils.isEmpty(message)){
            return "未知错误";
        }
        return message;
    }
}
