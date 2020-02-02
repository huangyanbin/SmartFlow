package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;

/**
 * 空入参事件
 * @param <R> 返回值
 */
@FunctionalInterface
public interface FirstEvent<R> extends Event<Void,R> {

     @Override
     default void run(Flow flow, Void aVoid, Await<R> await){
          run(flow,await);
     }


     /**
      * 简化run参数
      * @param flow 流
      * @param await 等待回调
      */
     void run(Flow flow, Await<R> await);

}
