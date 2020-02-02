package com.bin.david.flow.event;

import com.bin.david.flow.await.Await;
import com.bin.david.flow.transform.Converter;
import com.bin.david.flow.flow.Flow;

/**
 * 转换事件
 * @param <P> 入参
 * @param <R> 出参
 */
public class ConverterEvent<P,R> implements Event<P,R> {
    /**
     * 转换器
     */
    private Converter converter;
    /**
     * 代理事件
     */
    private Event<P,R> event;

    /**
     * 转换事件
     * @param event 事件
     * @param <P1> 入参
     * @param <R1> 出参
     * @return 返回代理事件
     */
    public static<P1, R1> Event<P1, R1> covert(Converter converter,final Event<P1, R1> event){
        if(converter != null) {
           return new ConverterEvent<>(converter,event);
        }
        return event;
    }

    /**
     * 转换事件
     * @param converter 转换器
     * @param event  事件
     */
    public ConverterEvent(Converter converter, Event<P,R> event) {
        this.converter = converter;
        this.event = event;
    }

    /**
     * 执行
     * @param flow 事件
     * @param p 入参
     * @param await 执行等待监听
     */
    @Override
    public void run(Flow flow, P p, Await<R> await) {
        converter.transform(() -> event.run(flow,p,await));
    }
}
