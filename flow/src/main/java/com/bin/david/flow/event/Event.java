package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;

/**
 * 事件
 * @param <P> 入参
 * @param <R> 出参
 */
@FunctionalInterface
public interface Event<P,R> {

    /**
     * 执行内容
     * @param flow 事件
     * @param p 入参
     * @param await 执行等待监听
     */
    void run(Flow flow, P p, Await<R> await);


}
