package com.bin.david.flow.flow;

import com.bin.david.eventtask.R;
import com.bin.david.flow.await.Await;
import com.bin.david.flow.event.Event;
import com.bin.david.flow.transform.Condition;

/**
 * 条件流
 * @param <P>
 */
public class ConditionFlow<P,R1,R2> extends Flow<P, Void> {


    protected ConditionFlow(Condition<P> condition, Flow<Void, R1> trueFlow, Flow<Void,R2> falseFlow) {
        super((flow, p, await) -> {
            if(condition != null && condition.condition(p)){
                trueFlow.start();
            }else{
                falseFlow.start();
            }
            await.exec(null);
        });
    }


}
