package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.transform.Condition;
import com.bin.david.flow.flow.Flow;

/**
 * 条件判断事件
 * @param <P> 入参
 * @param <?> 出参
 */
public class ConditionEvent<P,R> implements Event<P, R> {
    /**
     * 转换器
     */
    private Condition<P> condition;
    /**
     * true事件
     */
    private Event<P,R> trueEvent;

    /**
     * false事件
     */
    private Event<P,R> falseEvent;

    /**
     * 转换事件
     * @param <P1> 入参
     * @param condition 条件
     * @return 返回代理事件
     */
    public static<P1,R1> ConditionEvent<P1,R1> covert(Condition<P1> condition, final Event<P1, R1> trueEvent,
                                            final Event<P1, R1> falseEvent){
        return new ConditionEvent<>(condition,trueEvent,falseEvent);
    }

    /**
     * 条件事件
     * @param condition 条件
     * @param trueEvent true事件
     * @param falseEvent  false 事件
     */
    public ConditionEvent(Condition<P> condition, Event<P,R> trueEvent,
                          final Event<P,R> falseEvent) {
        this.condition = condition;
        this.trueEvent = trueEvent;
        this.falseEvent = falseEvent;

    }


    /**
     * 执行
     * @param flow 事件
     * @param p 入参
     * @param await 执行等待监听
     */
    @Override
    public void run(Flow flow, P p, Await<R> await) {
        if(condition != null && condition.condition(p)){
            trueEvent.run(flow,p, await);
        }else{
            falseEvent.run(flow,p, await);
        }
    }

}