package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.await.VoidAwait;
import com.bin.david.flow.flow.Flow;

/**
 * None事件
 * run完毕自动下一步
 */
@FunctionalInterface
public interface NoneEvent extends Event<Void, Void> {
    @Override
    default void run(Flow flow, Void aVoid, Await<Void> await) {
        run(flow);
        await.exec(null);


    }


    /**
     * 简化run参数
     *
     * @param flow 流
     */
    void run(Flow flow);
}
