package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;
import com.bin.david.flow.await.VoidAwait;

/**
 * Void事件
 */
@FunctionalInterface
public interface VoidEvent extends Event<Void, Void> {

     @Override
     default void run(Flow flow, Void aVoid, Await<Void> await){
          run(flow, () -> await.exec(null));
     }


     /**
      * 简化run参数
      * @param flow 流
      * @param await 等待回调
      */
     void run(Flow flow, VoidAwait await);

}
