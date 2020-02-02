package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;

/**
 * 转换事件
 * @param <P>入参
 * @param <R> 返回类型
 * @param <T> 转换类型
 */
public abstract class TransformEvent<P,R,T> implements Event<P, T> {

    protected Event<P,R> event;

    public TransformEvent(Event<P, R> event) {
        this.event = event;
    }



    @Override
    public void run(Flow flow, P p, Await<T> await) {
        event.run(flow, p, r -> await.exec(covert(flow,r)));
    }


    abstract T covert(Flow flow,R r);

}
