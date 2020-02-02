package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;

import java.util.Collection;

/**
 * 多事件转换成单事件（分发）
 * @param <P> 入参
 * @param <R>  出参
 * @param <C> 容器
 */
public class EachEvent<P,R,C extends Collection<R>> extends TransformEvent<P,C,R> {


    public EachEvent(Event<P, C> event) {
        super(event);
    }

    @Override
    public void run(Flow flow, P p, Await<R> await) {
        event.run(flow, p, rs -> {
            for(R r:rs){
                await.exec(r);
            }
        });
    }

    @Override
    R covert(Flow flow, C rs) {
        return null;
    }


}
