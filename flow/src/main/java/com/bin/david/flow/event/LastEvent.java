package com.bin.david.flow.event;


import com.bin.david.flow.await.Await;
import com.bin.david.flow.await.VoidAwait;
import com.bin.david.flow.flow.Flow;

/**
 * 没有返回值事件
 * @param <P> 入参
 */
@FunctionalInterface
public interface LastEvent<P> extends Event<P, Void> {


    @Override
    default  void run(Flow flow, P p, Await<Void> await){
        run(flow, p, () -> await.exec(null));
    }

    void run(Flow flow, P p, VoidAwait await);
}
