package com.bin.david.flow.await;

/**
 * 等待返回
 * @param <T> 返回值
 */
public interface Await<T> {
    /**
     *执行
     */
    void exec(T t);
}
