package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.flow.Flow;

/**
 * 返回事件(只需要入参，不管出参)
 * @param <P> 入参
 */
public interface ResultEvent<P> extends Event<P,Void> {

     @Override
     default void run(Flow flow, P result, Await<Void> await){
          run(flow,result);
     }

     void run(Flow<P, Void> flow, P result);

}
